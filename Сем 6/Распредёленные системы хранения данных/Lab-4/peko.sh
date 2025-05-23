#!/usr/bin/env bash
set -euo pipefail

PG1=147.45.40.78
PG2=87.120.36.97
PGPORT=5432
BOUNCER_PORT=6432
DB=labdb
APPUSER=labuser
APPPASS=labpass
REPLUSER=replicator
REPLPASS=replpass
DATA_DIR=/var/lib/postgresql/16/main
PATH_ADDITION="/usr/bin:/usr/lib/postgresql/16/bin"
slot="pg1_slot_$(date +%s)"

g="\033[32m[+]\033[0m"
r="\033[31m[×]\033[0m"
ok()   { printf '%(%F %T)T %b %s\n' -1 "$g" "$*"; }
fail() { printf '%(%F %T)T %b %s\n' -1 "$r" "$*"; }

# psql_b() {
#   PGPASSWORD="$APPPASS" psql "host=$PG1 port=$BOUNCER_PORT dbname=$DB user=$APPUSER" -qAt -c "$1"
# }

# psql_p2() {
#   PGPASSWORD="$APPPASS" psql -h "$PG2" -p "$PGPORT" -U "$APPUSER" -d "$DB" -qAt -c "$1"
# }

psql_b(){ PGPASSWORD=$APPPASS psql "host=$PG1 port=$BOUNCER_PORT dbname=$DB user=$APPUSER" -At -c "$1"; }

psql_p2(){
  out=$(PGPASSWORD=$APPPASS psql -h "$PG2" -p "$PGPORT" -U "$APPUSER" -d "$DB" -At -c "$1") || {
    fail "psql_p2 error while running: $1"
    exit 1
  }
  ok "$out"
}

psql_p2_bool() {
  PGPASSWORD=$APPPASS psql -h "$PG2" -p "$PGPORT" \
    -U "$APPUSER" -d "$DB" -At -c "SELECT pg_is_in_recovery();" \
  | awk '{print $1}'
}

ssh1(){ ssh root@"$PG1" "export PATH=$PATH_ADDITION:\$PATH; $*"; }
ssh2(){ ssh root@"$PG2" "export PATH=$PATH_ADDITION:\$PATH; $*"; }

ok "initial check"
row1=$(psql_b "SELECT id from streams limit 1") || { fail "write failed"; exit 1; }
ok "id=$row1"

# exit 0;

ok "stop pg1 and drop data dir"
ssh1 "systemctl stop postgresql && rm -rf $DATA_DIR"

ok "promote pg2 if still standbyyyyy"

# if [[ $(psql_p2 "SELECT pg_is_in_recovery();") == "t" ]]; then
#   ssh2 "rm -f $DATA_DIR/standby.signal && sudo -u postgres pg_ctl promote -D $DATA_DIR"
#   sleep 3
# fi

# [[ $(psql_p2 "SELECT pg_is_in_recovery()") == "f" ]] || { fail "pg2 promotion failed"; exit 1; }

status=$(psql_p2_bool)
ok "DEBUG: initial pg_is_in_recovery=[$status]" >&2

if [[ "$status" == "t" ]]; then
  ok "PG2 is standby, promoting..."
  ssh2 "pg_ctlcluster 16 main promote"
  until [[ "$(psql_p2_bool)" == "f" ]]; do
    sleep 1
  done
  ok "PG2 promotion complete"
fi

status=$(psql_p2_bool)
if [[ "$status" != "f" ]]; then
  fail "PG2 promotion failed (still in recovery)"
  exit 1
fi
ok "PG2 confirmed as primary"

ok "promote completed"

row1=$(psql_b "INSERT INTO streams(talent_id,title,viewers_k,stream_date) VALUES (1,'row1',111,now()) RETURNING id;")
ok "write through PgBouncer id=$row1"

ok "prepare slot on pg2"
ssh2 "sudo -u postgres psql -qAt 2>/dev/null -c \"SET client_min_messages=error; SELECT pg_drop_replication_slot('pg1_slot') WHERE EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name='pg1_slot');\""

ok "force checkpoint on pg2 & clean slot (if any)"
ssh2 "
  sudo -u postgres psql -d postgres -qAt -c \"
      SELECT pg_drop_replication_slot('$slot')
      WHERE EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name = '$slot');
      CHECKPOINT;
  \"
"

ok "basebackup to pg1"
ssh1 "
  rm -rf $DATA_DIR &&
  mkdir -p $(dirname $DATA_DIR) &&
  chown -R postgres:postgres $(dirname $DATA_DIR)

  sudo -u postgres env PGPASSWORD=$REPLPASS pg_basebackup \
       -h $PG2 \
       -D $DATA_DIR \
       -U $REPLUSER \
       -Fp -Xs -P -R \
       -C -S $slot -w

  systemctl start postgresql &&
  for i in {1..60}; do sudo -u postgres pg_isready -q && exit 0; sleep 1; done; exit 111
"

[ $? -eq 0 ] || { fail "pg1 failed to start"; exit 1; }

ok "pg1 standby ready"

ok "ensure pg2_slot exists on pg1"
ssh1 'sudo -u postgres psql -qAt -c "
  DO \$\$
  BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name = '\''pg2_slot'\'') THEN
      PERFORM pg_create_physical_replication_slot('\''pg2_slot'\'');
    END IF;
  END;
  \$\$;
"'

ok "promote pg1"
ssh1 "pg_ctlcluster 16 main promote"
# sleep 3

ok "granting roles"
ssh1 "
  sudo -u postgres psql -d postgres -c \"
    GRANT EXECUTE ON FUNCTION pg_read_binary_file(text)                            TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_read_binary_file(text, boolean)                   TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_read_binary_file(text, bigint, bigint)            TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_read_binary_file(text, bigint, bigint, boolean)   TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_ls_dir(text)                                      TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_ls_dir(text, boolean, boolean)                    TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_stat_file(text)                                   TO $REPLUSER;
    GRANT EXECUTE ON FUNCTION pg_stat_file(text, boolean)                          TO $REPLUSER;
  \"
"

ok "Checkpoint"
ssh1 "sudo -u postgres psql -c 'CHECKPOINT;'"

ok "switch WAL on pg1"
ssh1 "sudo -u postgres psql -q -c 'CHECKPOINT; SELECT pg_switch_wal();' > /dev/null 2>&1"

ok "rewind pg2"
ssh2 "
  systemctl stop postgresql 2>/dev/null
  sudo -u postgres env PGPASSWORD=$REPLPASS /usr/lib/postgresql/16/bin/pg_rewind \
    --target-pgdata=$DATA_DIR \
    --source-server=\"host=$PG1 port=$PGPORT user=$REPLUSER password=$REPLPASS dbname=postgres\" 2>/dev/null
  echo \"primary_conninfo = 'host=$PG1 port=$PGPORT user=$REPLUSER password=$REPLPASS'\" > $DATA_DIR/postgresql.auto.conf 2>/dev/null
  echo \"primary_slot_name = 'pg2_slot'\" >> $DATA_DIR/postgresql.auto.conf 2>/dev/null
  touch $DATA_DIR/standby.signal 2>/dev/null
  systemctl start postgresql 2>/dev/null
"

sleep 15

# row3=$(psql_b "INSERT INTO streams(talent_id,title,viewers_k,stream_date) VALUES (1,'row3',3,now()) RETURNING id") || { fail "final write failed"; exit 1; }
total=$(psql_b "SELECT count(*) FROM streams")

# ok "final write id=$row3"
ok "total rows=$total"
ok "cycle complete (pg1 primary, pg2 standby)"

set -euo pipefail

PGDATA=$HOME/fer7
ARCHIVE=$HOME/wal_archive
BASEBACKUP_DIR=$HOME/pitr_base
RESTORE_PGDATA=$HOME/fer7_pitr

PORT=9116
DB=wetwhitelaw
USR_DATA=appuser
USR_BKUP=postgres0
PW=1

log(){ printf '\033[1;32m[%s] %(%F %T)T\033[0m %s\n' "$1" -1 "$2"; }
die(){ log ERROR "$1"; exit 1; }

mkdir -p ~/wal_archive/

[ -f "$PGDATA/postgresql.conf" ] || die "PGDATA not found"
grep -q "^archive_mode *= *on" "$PGDATA/postgresql.conf" || die "archive_mode is OFF"

pg_ctl -D "$PGDATA" -w stop 2>/dev/null || true
pg_ctl -D "$RESTORE_PGDATA" -w stop 2>/dev/null || true
pg_ctl -D "$PGDATA" -o "-p $PORT" -w start

TS=$(PGPASSWORD="$PW" psql -At -h localhost -p $PORT -U $USR_DATA -d $DB -c "SELECT now()")
log INFO "target $TS"

log INFO "basebackup"
rm -rf "$BASEBACKUP_DIR"
PGPASSWORD="$PW" pg_basebackup -h 127.0.0.1 -p $PORT -D "$BASEBACKUP_DIR" \
  -Ft -z -P --wal-method=stream -U $USR_BKUP --checkpoint=fast


log INFO "clean demo rows"
PGPASSWORD="$PW" psql -h localhost -p $PORT -U $USR_DATA -d $DB -c \
"DELETE FROM accounts WHERE name LIKE 'Demo%';"

log INFO "insert demo rows"
PGPASSWORD="$PW" psql -h localhost -p $PORT -U $USR_DATA -d $DB -c \
"INSERT INTO accounts(name,balance) VALUES ('DemoA',777.77),('DemoB',888.88);"

log INFO "corrupt rows"
PGPASSWORD="$PW" psql -h localhost -p $PORT -U $USR_DATA -d $DB -c \
"UPDATE accounts SET account_id = account_id + 9000 WHERE name LIKE 'Demo%' AND account_id < 9000;"

log INFO "check corruption"
PGPASSWORD="$PW" psql -h localhost -p $PORT -U $USR_DATA -d $DB -c \
"SELECT account_id,name FROM accounts WHERE name LIKE 'Demo%';"

pg_ctl -D "$PGDATA" -w stop
log INFO "primary stopped"

log INFO "prepare restore cluster"
rm -rf "$RESTORE_PGDATA"
mkdir -p "$RESTORE_PGDATA"
tar -xf "$BASEBACKUP_DIR/base.tar.gz"   -C "$RESTORE_PGDATA"
tar -xf "$BASEBACKUP_DIR/pg_wal.tar.gz" -C "$RESTORE_PGDATA"
chmod -R 700 "$RESTORE_PGDATA"
cat > "$RESTORE_PGDATA/postgresql.auto.conf" <<EOF
restore_command='cp $ARCHIVE/%f %p'
recovery_target_time='$TS'
port=$PORT
EOF
touch "$RESTORE_PGDATA/recovery.signal"

log INFO "start PITR"
pg_ctl -D "$RESTORE_PGDATA" -w start

log INFO "verify result"
PGPASSWORD="$PW" psql -h localhost -p $PORT -U $USR_DATA -d $DB -c \
"SELECT account_id,name,balance FROM accounts WHERE name LIKE 'Demo%';"

log INFO "finished"

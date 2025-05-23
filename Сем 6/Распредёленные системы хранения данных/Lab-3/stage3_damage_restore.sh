set -euo pipefail

log() {
  local color reset level msg
  reset='\033[0m'
  case "$1" in
    INFO)  color='\033[1;32m'; level="INFO" ;;
    WARN)  color='\033[1;33m'; level="WARN" ;;
    ERROR) color='\033[1;31m'; level="ERROR" ;;
    *)     color='\033[0m';    level="LOG" ;;
  esac
  shift
  printf '%b[%s] %(%F %T)T%b %s\n' "$color" "$level" -1 "$reset" "$*"
}

PGDATA=$HOME/fer7
REMOTE=pg153-backup
BACKUPS=/var/db/postgres5/pgbackups/pg152_daily
NEW_TS_ROOT=$HOME/tblspc_new
PGPORT=9116

ssh -oBatchMode=yes "$REMOTE" 'echo OK' >/dev/null || {
  log ERROR "cannot SSH to $REMOTE"
  exit 1
}

if pg_ctl -D "$PGDATA" status &>/dev/null; then
  log INFO "Stopping running PostgreSQL…"
  pg_ctl -D "$PGDATA" -m fast -w stop
fi

log INFO "SIMULATING DISK FAILURE: removing $PGDATA and tablespaces"
rm -rf "$PGDATA"

if [[ -d $NEW_TS_ROOT ]]; then
  rm -rf "$NEW_TS_ROOT"
fi
mkdir -p "$NEW_TS_ROOT"

log INFO "Attempting to start PostgreSQL (should fail)…"
if pg_ctl -D "$PGDATA" start 2>/dev/null; then
  log ERROR "server managed to start – failure not reproduced"
  exit 1
fi
log INFO "Server failed as expected – cluster is gone."

LATEST=$(ssh "$REMOTE" "ls -1t $BACKUPS | head -n1")
log INFO "Restoring from backup $LATEST"

rsync -a --info=progress2 "$REMOTE:$BACKUPS/$LATEST/" "$PGDATA/"
chmod -R 700 "$PGDATA"

for tslink in "$PGDATA"/pg_tblspc/*; do
  [[ -L "$tslink" ]] || continue
  oid=$(basename "$tslink")
  src_on_backup="$BACKUPS/$LATEST/tblspc_${oid}"
  dst_on_local="$NEW_TS_ROOT/$oid"

  log INFO "  » tablespace OID $oid  →  $dst_on_local"
  mkdir -p "$dst_on_local"
  if ! rsync -a --info=progress2 "$REMOTE:$src_on_backup/" "$dst_on_local/"; then
    log WARN "$src_on_backup missing in backup – skipped"
  fi

  rm -f "$tslink"
  ln -s "$dst_on_local" "$tslink"
done
chmod -R 700 "$NEW_TS_ROOT"

grep -q "^port *= *$PGPORT" "$PGDATA/postgresql.conf" || \
  echo "port = $PGPORT" >> "$PGDATA/postgresql.conf"

grep -q "scram-sha-256" "$PGDATA/pg_hba.conf" || \
  echo "host all all 0.0.0.0/0 scram-sha-256" >> "$PGDATA/pg_hba.conf"

log INFO "Starting PostgreSQL…"
pg_ctl -D "$PGDATA" -w start

log INFO "Running test query on table accounts"
if ! PGPASSWORD="1" psql -h localhost -p "$PGPORT" -d wetwhitelaw -U appuser -c "SELECT * FROM accounts;"; then
  log ERROR "data unavailable"
  exit 1
fi

log INFO "RESTORE COMPLETED SUCCESSFULLY – database is up and healthy."

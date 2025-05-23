set -euo pipefail

PGDATA=$HOME/fer7
REMOTE=pg153-backup
DEST=/var/db/postgres5/pgbackups/pg152_daily
KEEP=14
DATE=$(date +%F)

LOGDIR=$HOME/pgbackupbin
mkdir -p "$LOGDIR"
LOG="$LOGDIR/pg_cold_backup.log"

{
  echo "=== $(date '+%F %T') backup started ==="

  pg_ctl -D "$PGDATA" -m fast stop 2>/dev/null || true

  rsync -a --delete --compress --info=progress2 \
        "$PGDATA/" "$REMOTE:$DEST/$DATE/"

  for oid in $(ls -1 "$PGDATA/pg_tblspc"); do
      real=$(readlink "$PGDATA/pg_tblspc/$oid") || continue
      rsync -a --delete --compress \
            "$real/" "$REMOTE:$DEST/$DATE/tblspc_${oid}/"
  done

  pg_ctl -D "$PGDATA" start

  ssh "$REMOTE" "
      cd $DEST &&
      total=\$(ls -1tr | wc -l)
      remove=\$((total - $KEEP))
      [ \$remove -gt 0 ] && ls -1tr | head -n \$remove | xargs -r rm -rf
  "

  echo "=== $(date '+%F %T') backup finished ==="

} # >>"$LOG" 2>&1

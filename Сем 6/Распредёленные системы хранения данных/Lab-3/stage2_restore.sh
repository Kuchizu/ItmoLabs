set -euo pipefail

PGDATA=$HOME/fer7
BACKUPS=/var/db/postgres5/pgbackups/pg152_daily

LATEST=$(ls -1t "$BACKUPS" | head -n1)
echo "Restoring from $LATEST …"

pg_ctl -D "$PGDATA" -m fast stop 2>/dev/null || true

rsync -a --delete "$BACKUPS/$LATEST/" "$PGDATA/"
chmod -R 700 "$PGDATA"

for SRC in "$BACKUPS/$LATEST"/tblspc_*; do
  [[ -e "$SRC" ]] || continue
  OID=${SRC##*/tblspc_}
  DST="$HOME/tblspc_$OID"

  mkdir -p "$DST"
  rsync -a --delete "$SRC/" "$DST/"
  chmod 700 "$DST"

  rm -f "$PGDATA/pg_tblspc/$OID"
  ln -s "$DST" "$PGDATA/pg_tblspc/$OID"
done

pg_ctl -D "$PGDATA" -w start
echo "Restore finished – PostgreSQL is up"

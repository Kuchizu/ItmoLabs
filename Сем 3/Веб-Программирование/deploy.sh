ssh -p 2222 s373746@se.ifmo.ru 'rm -rf ~/public_html/Lab-1/'
scp -P 2222 -r Lab-1 s373746@se.ifmo.ru:~/public_html

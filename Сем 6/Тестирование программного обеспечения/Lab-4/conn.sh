# -f: Background
# -N: Not remote commands
# -L: 8080 ---> 8083

ssh -f -N -L 8083:stload.se.ifmo.ru:8080 s373746@helios.cs.ifmo.ru -p 2222
if [ -d "lab0" ]; then
	chmod -R 777 lab0
	rm -R lab0
fi

if [ ! -f "/tmp/s373746" ]; then
	touch /tmp/s373746
fi

mkdir lab0
cd lab0

mkdir chandelure3
mkdir chandelure3/eevee
touch chandelure3/psyduck
mkdir chandelure3/chikorita
touch chandelure3/charmeleon
mkdir chandelure3/shelmet

mkdir gastrodon7
touch gastrodon7/foongus
touch gastrodon7/wurmple
touch gastrodon7/manectric
touch gastrodon7/mantyke
mkdir gastrodon7/anorith
mkdir gastrodon7/lileep

mkdir krabby3
touch krabby3/bronzong
touch krabby3/combee
touch krabby3/silcoon

touch lillipup7
touch nuzleaf4
touch tepig6

echo "Развитые способности swift Swim" > chandelure3/psyduck
echo "Тип покемона
FIRE NONE" > chandelure3/charmeleon
echo "Тип покемона
GRASS POISON" > gastrodon7/foongus
echo "способности
Swarm Shield Dust Obvilious" > gastrodon7/wurmple
echo "Живет Forest
Grassland" > gastrodon7/manectric
echo "Тип покемона WATER FLYING" > gastrodon7/mantyke
echo "Тип диеты
Nullivore" > krabby3/bronzong
echo "weight=12.1 height=12.0 akt=3
def=4" > krabby3/combee
echo "Способности Harden" > krabby3/silcoon
echo "Способности Leer Tackle
Odor Sleuth Bite Helping Hand Take Down Work Up Crunch Roar Retalite
Reversal Last Resort Giga Impact" > lillipup7
echo "Способности Overgrow Dark
Art Chlorophyll Early Bird" > nuzleaf4
echo "satk=5 sdef=5 spd=5" > tepig6

chmod 755 chandelure3
chmod 550 chandelure3/eevee
chmod u=,g=,o=r chandelure3/psyduck
chmod 570 chandelure3/chikorita
chmod u=,g=rw,o=w chandelure3/charmeleon
chmod u=wx,g=wx,o=rx chandelure3/shelmet
chmod u=wx,g=rw,o=rx chandelure3/shelmet
chmod u=rw,g=w,o= gastrodon7/foongus
chmod u=r,g=,o=r gastrodon7/wurmple
chmod u=rw,g=w,o=w gastrodon7/manectric
chmod u=rw,g=w,o=r gastrodon7/mantyke
chmod 555 gastrodon7/anorith
chmod u=rx,g=rwx,o=wx gastrodon7/lileep
chmod u=rwx,g=wx,o=wx krabby3
chmod u=,g=r,o=rw krabby3/bronzong
chmod u=r,g=,o= krabby3/combee
chmod u=r,g=,o= krabby3/silcoon
chmod u=r,g=r,o=r lillipup7
chmod u=,g=rw,o=w nuzleaf4
chmod u=rw,g=r,o= tepig6

cat tepig6 > gastrodon7/manectrictepig
ln ~/lab0/lillipup7 chandelure3/psyducklillipup
chmod +w chandelure3/chikorita
cp tepig6 chandelure3/chikorita
chmod +r krabby3/bronzong
cp -R krabby3 chandelure3/chikorita
cat gastrodon7/foongus krabby3/bronzong > tepig6_35
ln -s ~/lab0/lillipup7 chandelure3/charmeleonlillipup
ln -s ~/lab0/chandelure3 Copy_86


grep -rl . 2>/tmp/s373746 | grep 7$ | wc -m | sort -r

ls -Rl 2>/tmp/s373746 | grep e$ | sort -k 1

cat -n krabby3/* 2>&1 | sort -r

cat -n lillipup7 2>/tmp/s373746 | grep -v t$

ls -Rltr 2>/tmp/s373746 | grep 7$ | tail -4

cat -n **/m* ./m* | sort

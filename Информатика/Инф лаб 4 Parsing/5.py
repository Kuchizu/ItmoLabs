# JSON to CSV

from itertools import zip_longest

csv = ''
_arr = 0
_genarg = False
bazis, loctemp = {}, {}

def ext(*raw) -> str | list:
    if len(raw) == 1:
        return raw[0].strip().strip(',').strip('"')
    else:
        return [_.strip().strip().strip(',').strip('"') for _ in raw]

with open('schedule.json', encoding='UTF-8') as f:
    for line in f.readlines():
        line = line.strip()

        if '[' in line:
            _arr += 1

        if ']' in line:
            _arr -= 1

        if '}' in line:
            _genarg = False

        if line == '{':
            _genarg = _arr
            continue

        if _genarg:
            key, arg = ext(*line.split('": '))

            if arg == 'null':
                arg = '\t'
            if loctemp.get(key):
                loctemp[key] += [f'"{arg}"']
            else:
                loctemp[key] = [f'"{arg}"']

        if ':' in line and not _arr:
            key, arg = ext(*line.split('": '))
            bazis[key] = f'"{arg}"'

csv += '\t'.join(loctemp.keys()) + '\n'
for arg in zip_longest(*loctemp.values(), fillvalue='\t'):
    csv += ', '.join(arg) + '\n'

csv += '\n'
csv += '\t'.join(bazis.keys()) + '\n'
csv += '\t'.join(bazis.values()) + '\n'
print(csv)

with open('schedule.csv', 'w') as f:
    f.write(csv)

print('Saved in schedule.csv')

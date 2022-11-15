# JSON to XML

import os
import time
import re

xml = '<?xml version="1.0" encoding="UTF-8"?>'

_layer = 0
_arr = 0
_genarg = False
_isnull = False
DEBUG = True
gentime = 0.5
tab = chr(32) * 3
bazis = {}


def ext(*raw) -> str | list:
    if len(raw) == 1:
        return raw[0].strip().strip(',').strip('"')
    else:
        return [_.strip().strip().strip(',').strip('"') for _ in raw]


def color(raw: str | int, color: str):
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    ENDC = '\033[0m'
    GREY = '\033[90m'
    MAGENTA = '\033[95m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'

    return f'{eval(color)}{raw}{ENDC}'


with open('schedule.json', encoding='UTF-8') as f:
    for line in re.split(r'\n', f.read()):
        line = line.strip()

        if '[' in line:
            art = ext(re.split(r':\s\[', line)[0])
            _arr += 1
            _layer += 1
            xml += f'\n{tab * _layer}<{art} type="list">'

        if ']' in line:
            _arr -= 1
            xml += f'\n{tab * _layer}</{art}>'
            _layer -= 1

        if line == '{':
            if _arr:
                _layer += 1
                _genarg = True
                xml += f'\n{tab * _layer}<element type="dict">'
                _layer += 1
                continue
            else:
                xml += '\n<root>'

        if '}' in line:
            if _genarg:
                _genarg = False
                _layer -= 1
                xml += f'\n{tab * _layer}</element>'
                _layer -= 1
            else:
                _layer -= 1
                xml += '\n</root>'

        if _genarg:
            key, arg = re.split('": ', re.findall(r'^"\w*":\s"?[а-яА-Я\w\s.-0-9:]*"?', line)[0])
            if arg == 'null':
                _isnull = True
                xml += f'\n{tab * _layer}<{key} null="true" />'
            else:
                _isnull = False
                xml += f'\n{tab * _layer}<{key}>{arg}</{key}>'


        if ':' in line and not _arr:
            key, arg = ext(*re.split(r'\w": ', line))
            xml += f'\n{tab * _layer}<{key}>{arg}</{key}>'

        if not DEBUG:
            continue

        os.system('clear')
        print(
            '\n'.join(xml.split('\n')[:-1]),
            '\n', color(xml.split('\n')[-1], 'RED'),
            color('Layer: ' + str(_layer), 'CYAN'),
            '\n\n',
            color('Line: ' + str(xml.count('\n')), 'YELLOW'),
            '\n\n\n\n\n      ',
            color('LOGS:', 'RED'),
            color('Line: ', 'MAGENTA'), xml.count('\n'),
            color('Layer: ', 'MAGENTA'), _layer,
            color('Arr: ', 'MAGENTA'), _arr,
            end=' '
        )
        print(
            (color('Gen Arg?: ', 'RED') + str(_genarg)) if _genarg else '',
            (color('Isnull?: ', 'RED') + str(_isnull)) if _isnull else ''
        )
        time.sleep(gentime)

time.sleep(gentime)

os.system('clear')

with open('schedule.xml', 'w') as f:
    f.write(xml)

print(xml, '\n\n', time.ctime(), color('SUCCESSFUL', 'GREEN'), '   Saved to ', color('schedule.xml', 'RED'))

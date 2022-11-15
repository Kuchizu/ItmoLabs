from re import findall

print(*(f"{i}. {len(findall(r':-{O', x))}" for i, x in enumerate(open('tests1').readlines(), start=1)), sep='\n')

print(*((f'{i}. Не хайку. Должно быть 3 строки.' if len(x.split('/')) != 3 else f'{i}.', 'Хайку!' if list(map(lambda y: len(findall('[аоуиеАОУИЕ]', y)), x.split('/'))) == [5, 7, 5] else f'{i}. Не хайку. Должно быть 3 строки.') for i, x in enumerate(open('tests2').readlines())), sep='\n')

print(*list(map(lambda x: x[0], sorted(filter(lambda y: y[1] == 1, map(lambda x: (x, len(findall('[ауоиэыяюеёАУОИЭЫЯЮЕЁ]', ''.join(set(x))))), findall(r'\b\w*[ауоиэыяюеёАУОИЭЫЯЮЕЁ]\w*\b', test.split(';')[0]))), key=lambda z: len(z[0]))) for test in open('tests3').readlines())[0], sep='\n')


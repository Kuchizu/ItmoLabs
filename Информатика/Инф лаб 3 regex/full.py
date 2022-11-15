from re import findall, match

# 1.
# with open('tests1') as f:
#     for i, x in enumerate(f.readlines(), start=1):
# test, res = x.split(';')
# x = input()
# print(f"{len(findall(r':-{O', x))}")

# print('\n', '*' * 20, ' ' * 4, '*' * 20, '\n')
#
# # 2.
# with open('tests2') as f:
#     haiku = [5, 7, 5]
#     for i, x in enumerate(f.readlines(), start=1):
#         test, res = x.split(';')
#         for j, y in enumerate(test.split('/')):
#             # print(len(findall('[ауоиэыяюеёАУОИЭЫЯЮЕЁ]', y)))
#             if not len(findall('[ауоиэыяюеёАУОИЭЫЯЮЕЁ]', y)) == haiku[j]:
#                 if len(test.split('/')) != 3:
#                     print(f'{i}.', 'Не хайку. Должно быть 3 строки.')
#                 else:
#                     print(f'{i}.', 'Не хайку.')
#                 break
#         else:
#             print(f'{i}.', 'Хайку!')
#
# print('\n', '*' * 20, ' ' * 4, '*' * 20, '\n')
#
# # 3.
# with open('tests3') as f:
#     for i, x in enumerate(f.readlines(), start=1):
#         print(f'{i}.')
#         test, res = x.split(';')
#         resp = findall(r'\b\w*[ауоиэыяюеёАУОИЭЫЯЮЕЁ]\w*\b', test)
#         q = map(lambda x: (x, len(findall('[ауоиэыяюеёАУОИЭЫЯЮЕЁ]', ''.join(set(x))))), resp)
#         print(*map(lambda x: x[0], sorted(filter(lambda y: y[1] == 1, q), key=lambda z: len(z[0]))), sep='\n')
#         print()

print(match(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", input()))

import openpyxl
import os

xfile = openpyxl.load_workbook('123.xlsx')
sheet = xfile.get_sheet_by_name('Лист1')

os.system('clear')

print(sheet['A1'].value)
print(sheet['A2'].value)
print(sheet['A3'].value)
print(sheet['A4'].value)


exit()

a, c = input('A: '), input('C: ')

sheet['C1'] = a
sheet['C2'] = c

print()

print(bin(int(a))[2:].rjust(16, '0'))
print('+')
print(bin(int(c))[2:].rjust(16, '0'))
print('=')
print(bin(int(a) + int(c))[2:].rjust(16, '0'))

xfile.save('pyedited.xlsx')

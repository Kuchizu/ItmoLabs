ORG 0x0050
x: word 0x0003
y: word 0x0002
arr_now: word 0x0500
arr_size: word 0x0000
i: word 0x0000
j: word 0x0000
word 0x0000
sum: word 0x0000
tmp: word 0x0000

start:
  LD i
  CMP x
  BEQ exit

  LD (arr_now)+
  ST tmp

  LD i
  ADD j
  ROR

  BCC next

  LD tmp
  ADD sum
  ST sum

next:
  LD j
  CMP y
  BEQ iinc

  INC
  ST j
  JUMP start

iinc:
  CLA
  ST j
  LD i
  INC
  ST i
  JUMP start


exit:
  HLT

ORG 0x500
WORD 0X0001 ; [0, 0]    0
WORD 0X0002 ; [0, 1]    1  `
WORD 0X0003 ; [0, 2]    2
WORD 0X0004 ; [1, 0]    1  `
WORD 0X0005 ; [1, 1]    2
WORD 0X0006 ; [1, 2]    3  `
WORD 0X0007 ; [2, 0]    2
WORD 0X0008 ; [2, 1]    3  `
WORD 0X0009 ; [2, 2]    4

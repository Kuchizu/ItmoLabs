# 5.S Program for counting 1 in n_2:
# 1
#
# 1

.globl main

main:
    call readi32
    movl $0, %ecx
    movl $1, %edx
    movl $32, %esi

.loop:
    testl %edx, %eax
    jz .bitzero

    incl %ecx

.bitzero:
    shll %edx
    decl %esi
    jnz .loop

    movl %ecx, %eax
    call writei32
    call finish

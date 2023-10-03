# 2.S Program for:
# D
# A - 2 * B + 17 * C

.section .text
.global main

main:
    call readi32
    mov %eax, %ebx # A

    call readi32
    mov %eax, %ecx # B

    call readi32
    mov %eax, %edx # C

    call readi32
    call writei32
    mov %eax, %edi # D

    sal $1, %ecx

    movl %edx, %eax
    sal $4, %edx
    addl %eax, %edx

    subl %ecx, %ebx
    addl %edx, %ebx

    movl %ebx, %eax

    call writei32

    call nl
    call finish

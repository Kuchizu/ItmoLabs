# 2.2.S Program for R = A * B
# 2 3
#
# 6

.section .data
A:  .int 0
B:  .int 0
R:  .int 0

.globl process, A, B, R
.section .text

process:
    pushl %ebx
    pushl %edx

    movl A, %eax
    movl B, %ebx
    movl $0, %edx

.loop:
    testl %ebx, %ebx
    jz .done

    testl $1, %ebx
    jz .skip_add

    addl %eax, %edx

.skip_add:
    shll $1, %eax
    shrl $1, %ebx
    jmp .loop

.done:
    movl %edx, R
    popl %edx
    popl %ebx
    ret

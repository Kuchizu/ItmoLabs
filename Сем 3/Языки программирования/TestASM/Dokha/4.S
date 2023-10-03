# 4.S Program for:
# 1 4 7 2 6 ...
#
# sum(even)
# sum(odd)

.globl main

main:
    movl $0, %edi
    movl $0, %edx

.loop:
    call readi32
    jc end

    testl $1, %eax
    jnz odd

    addl %eax, %edi
    jmp .loop


odd:
    addl %eax, %edx
    jmp .loop

end:
    movl %edi, %eax
    call writei32

    movl %edx, %eax
    call writei32

    call finish

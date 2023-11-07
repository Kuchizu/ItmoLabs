# 3.S Program for:
# 5
#
# 5 4 3 2 1

.global main

main:
    call readi32

    testl %eax, %eax
    js end

    movl %eax, %ecx

    loop:
        movl %ecx, %eax
        call writei32
        decl %ecx
        testl %ecx, %ecx
        jns loop

    end:
        call finish

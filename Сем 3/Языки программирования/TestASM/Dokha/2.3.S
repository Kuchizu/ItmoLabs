.att_syntax noprefix
.text
.globl partition

partition:
    pushl %ebp
    movl %esp, %ebp

    pushl %ebx
    pushl %edx
    movl 8(%ebp), %ebx
    movl 12(%ebp), %esi
    movl 16(%ebp), %edi

    leal (%ebx), %edx
    leal (%ebx, %esi, 2), %ecx

    dec %ecx
    sal $1, %esi

.partition_loop:
    find_smaller:
        cmpw %di, (%edx)
        jle find_smaller_next
        jmp check_ptrs

    find_smaller_next:
        add $2, %edx
        cmp %edx, %ecx
        jbe find_smaller

    find_larger:
        cmpw %di, (%ecx)
        jge find_larger_next
        jmp check_ptrs

    find_larger_next:
        sub $2, %ecx
        cmp %edx, %ecx
        jae find_larger

    check_ptrs:
        cmp %edx, %ecx
        jg partition_done

    swap:
        movw (%edx), %ax
        movw (%ecx), %bx
        movw %bx, (%edx)
        movw %ax, (%ecx)
        add $2, %edx
        sub $2, %ecx
        jmp .partition_loop

partition_done:
    leal (%ebx), %eax
    sub %eax, %edx
    sar $1, %edx

    popl %edx
    popl %ebx
    popl %ebp
    ret

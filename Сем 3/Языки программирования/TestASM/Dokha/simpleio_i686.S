/* -*- asm -*- */

.code32

        .text
_form1: .asciz  "%d"
        .text
        .align  16
        .global readi32
readi32:
        pushl   %ebp
        movl    %esp, %ebp

        subl    $4, %esp
        and     $-16, %esp
        leal    -4(%ebp), %eax

        push    %ecx
        push    %edx

        sub     $8, %esp
        mov     %eax, 4(%esp)
        movl    $_form1, (%esp)
        call    scanf
        add     $8, %esp
        movl    %eax, %ecx
        movl    -4(%ebp), %eax
        clc
        cmpl    $1, %ecx
        je      readi32x
        stc
readi32x:       
        pop     %edx
        pop     %ecx
        movl    %ebp, %esp
        popl    %ebp
        ret

        .text
_form2: .asciz  "%d\n"
        .text
        .align  16
        .global writei32
writei32:       
        pushl   %ebp
        movl    %esp, %ebp
        and     $-16, %esp
        push    %ecx
        push    %edx

        sub     $8, %esp
        mov     %eax, 4(%esp)
        movl    $_form2, (%esp)
        call    printf
        add     $4, %esp

        push stdout
        call fflush
        add $4, %esp

        pop     %eax
        pop     %edx
        pop     %ecx

        movl    %ebp, %esp
        popl    %ebp
        ret

        .align  16
        .global nl
nl:
        pushl   %ebp
        movl    %esp, %ebp
        and     $-16, %esp
        push    %ecx
        push    %edx

        sub     $8, %esp
        movl    $'\n', (%esp)
        call    putchar
        add     $8, %esp
        pop     %edx
        pop     %ecx
        movl    %ebp, %esp
        popl    %ebp
        ret

        .text
        .align  16
        .global finish
finish:
        and     $-16, %esp
        sub     $16, %esp
        movl    $0, (%esp)
        call    exit

        .text
_form3: .asciz  "%lli"
        .align  16
        .global readi64
readi64:
        pushl   %ebp
        movl    %esp, %ebp
        sub     $8, %esp
        and     $-16, %esp
        push    %ecx

        leal    -8(%ebp), %eax
        sub     $12, %esp
        mov     %eax, 4(%esp)
        movl    $_form3, (%esp)
        call    scanf
        add     $12, %esp
        movl    %eax, %ecx
        movl    -8(%ebp), %eax
        movl    -4(%ebp), %edx
        clc
        cmpl    $1, %ecx
        je      readi64x
        stc
readi64x:       
        pop     %ecx
        movl    %ebp, %esp
        popl    %ebp
        ret

        .text
_form4: .asciz  "%lld\n"
        .align  16
        .global	writei64
writei64:       
        pushl   %ebp
        movl    %esp, %ebp
        and     $-16, %esp
        push    %ecx

        sub     $12, %esp
        mov     %edx, 8(%esp)
        mov     %eax, 4(%esp)
        movl    $_form4, (%esp)
        call    printf
        add     $4, %esp

        push stdout
        call fflush
        add $4, %esp

        pop     %eax
        pop     %edx
        pop     %ecx

        movl    %ebp, %esp
        popl    %ebp
        ret

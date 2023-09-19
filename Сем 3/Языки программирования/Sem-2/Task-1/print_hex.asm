section .data
codes:
    db '0123456789ABCDEF'

section .text
    global print_hex

print_hex:
    ; переданный аргумент находится в rdi
    mov rax, rdi

    mov rdi, 1
    mov rdx, 1
    mov rcx, 64

.loop:
    push rax
    sub rcx, 4
    sar rax, cl
    and rax, 0xf

    lea rsi, [codes + rax]
    mov rax, 1
    push rcx
    syscall
    pop rcx
    pop rax

    test rcx, rcx
    jnz .loop

    ret

global _start
_start:
    ; вывод трех чисел с помощью функции print_hex
    mov rdi, 0x1122334455667788
    call print_hex

    mov rdi, 0x1234567890ABCDEF
    call print_hex

    mov rdi, 0xAABBCCDDEEFF0011
    call print_hex

    ; завершаем программу
    mov rax, 60
    xor rdi, rdi
    syscall

section .data
codes:
    db   '0123456789ABCDEF'

section .text
global _start

print_hex:
    mov  rdi, 1
    mov  rdx, 1
    mov  rcx, 64

.loop:
    push rax
    sub  rcx, 4
    sar  rax, cl
    and  rax, 0xf

    lea  rsi, [codes + rax]
    mov  rax, 1

    push  rcx
    syscall
    pop  rcx

    pop  rax
    test rcx, rcx
    jnz .loop

    ret

print_variables:
    sub rsp, 24  ; Выделить память на стеке под 3 переменные

    ; Инициализация переменных
    mov qword [rsp], 0xaa      ; первая переменная
    mov qword [rsp+8], 0xbb    ; вторая переменная
    mov qword [rsp+16], 0xff   ; третья переменная

    ; Вывод первой переменной
    mov rax, [rsp]
    call print_hex

    ; Вывод второй переменной
    mov rax, [rsp+8]
    call print_hex

    ; Вывод третьей переменной
    mov rax, [rsp+16]
    call print_hex

    ; Освобождение памяти на стеке
    add rsp, 24
    ret

_start:
    call print_variables

    ; Завершение программы
    mov  rax, 60
    xor  rdi, rdi
    syscall

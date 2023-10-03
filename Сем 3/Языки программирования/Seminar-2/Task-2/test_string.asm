; print_string.asm
section .data
message: db  'Hello, world!', 10, 0

section .text
global _start

exit:
    mov  rax, 60
    xor  rdi, rdi
    syscall

string_length:
    mov  rax, rdi
  .counter:
    cmp  byte [rdi], 0
    je   .end
    inc  rdi
    jmp  .counter
  .end:
    sub  rdi, rax
    mov  rax, rdi
    ret

print_string:

    call string_length

    mov rsi, rax
    mov rdi, message

    mov  rdx, rsi
    mov  rsi, rdi
    mov  rax, 1
    mov  rdi, 1
    syscall
    ret

_start:
    mov  rdi, message
    call print_string
    call exit

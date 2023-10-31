%define STR_LENGTH 256
%define ERR_EXIT 1

%include "lib.inc"
%include "dict.inc"
%include "words.inc"

section .rodata
    err_too_long:     db "String is too long (>255).", 0
    err_not_fnd:      db "String not found.", 0

section .bss
    input: resb STR_LENGTH-1

section .text
global _start

_start:
    lea rsi, [input]
    mov rdx, STR_LENGTH
    syscall

    test rax, rax
    jl .error_exit
    cmp rax, STR_LENGTH
    jge .error_length

    lea rdi, [input]
    mov rsi, test
    call find_word
    test rax, rax
    jz .error_notfound

    mov rdi, rax
    call string_length
    lea rdi, [rdi+rax+1]
    call print_string
    call print_newline
    xor rdi, rdi
    jmp exit


.error_length:
    mov rdi, err_too_long
    jmp .error_exit

.error_notfound:
    mov rdi, err_not_fnd

.error_exit:
    call print_error
    mov rdi, ERR_EXIT

%include "words.inc"

%define buff_size 256
%define psize 8

section .data
not_found: db "Key not found.", 0
key_too_long: db "Key is too long (>255).", 0

section .bss
buffer resb 256

section .text

global _start

extern exit
extern read_word
extern print_string
extern find_word
extern print_error
extern print_newline
extern string_length

_start:
    mov rdi, buffer
    mov rsi, buff_size
    call read_word

    test rax, rax
    jz .key_too_long

    mov rdi, rax
    mov rsi, elem
    call find_word

    test rax, rax
    jz .key_not_found

    mov rdi, rax
    add rdi, psize
    push rdi
    call string_length

    pop rdi
    lea rdi, [rdi+rax+1]
    call print_string

    jmp .exit

    .key_too_long:
        mov rdi, key_too_long
        call print_error
        jmp .exit
    
    .key_not_found:
        mov rdi, not_found
        call print_error

    .exit:
        xor rdi, rdi
        call exit


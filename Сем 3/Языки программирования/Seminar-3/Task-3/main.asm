%include "lib.inc"

section .text
global _start

_start:
    mov rdi, 0x1122334455667788
    call print_hex
    call exit

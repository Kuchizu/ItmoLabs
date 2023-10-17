%define O_RDONLY 0
%define PROT_READ 0x1
%define MAP_PRIVATE 0x2
%define SYS_WRITE 1
%define SYS_OPEN 2
%define SYS_MMAP 9
%define SYS_EXIT 60
%define FD_STDOUT 1
%define SYS_MUNMAP 11
%define SYS_CLOSE 3
%define SYS_FSTAT 5
%define STAT_SIZE 144
%define OFFSETOF_ST_SIZE 48
%define SIZEOF_OFF_T 8

section .text
global print_file
global print_string

print_string:
    push rdi
    call string_length
    pop  rsi
    mov  rdx, rax
    mov  rax, SYS_WRITE
    mov  rdi, FD_STDOUT
    syscall
    ret

string_length:
    xor  rax, rax
.loop:
    cmp  byte [rdi+rax], 0
    je   .end
    inc  rax
    jmp .loop
.end:
    ret

print_substring:
    mov  rdx, rsi
    mov  rsi, rdi
    mov  rax, SYS_WRITE
    mov  rdi, FD_STDOUT
    syscall
    ret

print_file:
    push rbx
    push r12

    mov  rax, SYS_OPEN
    mov  rsi, O_RDONLY
    mov  rdx, 0
    syscall
    mov  rbx, rax

    sub  rsp, STAT_SIZE

    mov  rax, SYS_FSTAT
    mov  rdi, rbx
    mov  rsi, rsp
    syscall

    mov  rax, [rsp + OFFSETOF_ST_SIZE]
    mov  r12, rax

    xor  rdi, rdi
    mov  rsi, r12
    mov  rdx, PROT_READ
    mov  r10, MAP_PRIVATE
    mov  r8, rbx
    xor  r9, r9
    mov  rax, SYS_MMAP
    syscall

    mov  rdi, rax
    mov  rsi, r12
    call print_substring

    mov  rax, SYS_MUNMAP
    mov  rdi, rax
    mov  rsi, r12
    syscall

    mov  rax, SYS_CLOSE
    mov  rdi, rbx
    syscall

    add rsp, STAT_SIZE
    pop r12
    pop rbx
    ret

%macro CREATE_STRING 1-*
    %assign idx 1
    joined_string:
	    %rep %0
	        db %1
	        %rotate 1
	        %if idx < %0
	            db ", "
	        %endif
	        %assign idx idx+1
	    %endrep
    db 0
    joined_len equ $ - joined_string
%endmacro

section .data
    CREATE_STRING "HELLO", "2", "3"

section .text
    global _start

_start:
    mov rax, 1
    mov rdi, 1
    mov rsi, joined_string
    mov rdx, joined_len
    syscall

    mov rax, 60	
    mov rdi, 0
    syscall

%macro CREATE_STRING 1-*
    %assign idx 1
    %rep %0
        db %1
        %rotate 1
        %if idx < %0
            db ", "
        %endif
        %assign idx idx+1
    %endrep
%endmacro

section .data
	message: db "Hello", 10, 0
	CREATE_STRING "a", "b", "c"

section .text
	global _start

_start:
	mov rax, 1
	mov rdi, 1
	mov rsi, message
	mov rdx, 6
	syscall

	mov rax, 60
	mov rdi, 0
	syscall

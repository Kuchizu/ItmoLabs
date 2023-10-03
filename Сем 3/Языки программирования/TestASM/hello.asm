; hello.asm

section .data
	message: db "Hello", 10, 0

section .text
	global _start

_start:
	mov rax, 1
	mov rdx, 1
	mov rsi, a
	mov rdi, 1
	syscall

	mov rax, 60
	mov rdi, 0
	syscall

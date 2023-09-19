; test.asm

section .data
	message: db 'Hello wtf', 10

section .text
	global _start

exit:
	mov rax, 60
	xor rdi, rdi
	syscall

print_hello:
	mov rax, 1
	mov rdi, 1
	mov rsi, message
	mov rdx, 10
	syscall
	ret

sum:
	mov rax, rdi
	add rsi, rax
	ret

_start:
	call print_hello
	call print_hello
	call exit

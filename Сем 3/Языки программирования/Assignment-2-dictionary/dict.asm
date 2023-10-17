extern string_equals
global find_word

find_word:
	%assign addr_size 8
	push r12
	push r13
	mov r12, rdi
	mov r13, rsi

    .check_key:
	mov rdi, [r13]
	test rdi, rdi
	je .end_of_dict
	add rdi, addr_size
	mov rsi, r12
	call string_equals
	test rax, rax
	jne .found
	mov r13, [r13]
	jmp .check_key

    .found:
	mov rax, [r13]
	jmp .end

    .end_of_dict:
	xor rax, rax

    .end:
	pop r13
	pop r12
	ret

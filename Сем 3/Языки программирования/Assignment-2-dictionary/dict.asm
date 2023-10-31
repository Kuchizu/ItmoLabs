extern string_equals
global find_word

find_word:
    push rsi      ; store callee-saved register
    mov rcx, rsi  ; store matching string address

    .loop:
        add rcx, 8          ; skipping value of pointer of previous element
        mov rsi, rcx        ; now here is an address of dictionary value
        push rcx
        call string_equals  ; compare with rdi string, result in rax
        pop rcx

        test rax, rax       ; 1 -- equals
        jnz .equals         ; 0 -- not equals

        sub rcx, 8          ; store value of pointer of previous element again
        mov rcx, [rcx]      ; switch to next element

        test rcx, rcx       ; pointer state end of dictionary
        jz .not_equals      ; end searching

        jmp .loop


    .equals:
        mov rax, rsi        ; return dictionary address of value
        jmp .return
    .not_equals:
        xor rax, rax
    .return:
        pop rsi             ; restore callee-saved
        ret

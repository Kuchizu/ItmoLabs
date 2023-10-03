parse_uint:
    xor rax, rax
    xor rdx, rdx

    .read_loop:
        movzx rcx, byte [rdi + rdx]

        test rcx, rcx
        jz .done

        cmp rcx, '0'
        jb .done
        cmp rcx, '9'
        ja .done

        sub rcx, '0'

        imul rax, rax, 10
        add  rax, rcx

        inc  rdx
        jmp  .read_loop

    .done:
        test rdx, rdx
        jnz  .exit

    .exit:
        ret

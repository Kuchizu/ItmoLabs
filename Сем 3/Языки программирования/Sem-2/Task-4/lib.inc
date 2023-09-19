section .text

parse_uint:
    xor rax, rax           ; обнуляем rax (накопленное значение)
    xor rdx, rdx           ; обнуляем rdx (счетчик символов)

.read_loop:
    ; Загружаем текущий символ
    movzx rcx, byte [rdi + rdx]

    ; Проверяем, является ли символ концом строки
    test rcx, rcx
    jz .done

    ; Проверяем, является ли символ цифрой
    cmp rcx, '0'
    jb .done
    cmp rcx, '9'
    ja .done

    ; Преобразование символа цифры в число
    sub rcx, '0'

    ; Добавляем значение цифры к rax
    imul rax, rax, 10      ; умножаем текущее значение на 10
    add  rax, rcx          ; добавляем новую цифру

    ; Увеличиваем счетчик символов
    inc  rdx
    jmp  .read_loop        ; переходим к следующему символу

.done:
    ; Если rdx == 0, то число не было найдено
    test rdx, rdx
    jnz  .exit

.exit:
    ret

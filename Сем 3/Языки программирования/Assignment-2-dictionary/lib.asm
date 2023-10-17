section .text

global exit
global string_length
global print_string
global print_char
global print_newline
global print_uint
global print_int
global string_equals
global read_char
global read_word
global parse_uint
global parse_int
global string_copy
global print_error


; Принимает код возврата и завершает текущий процесс
exit:
    mov rax, 60
    syscall

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    mov  rax, rdi
  .counter:
    cmp  byte [rdi], 0
    je   .end
    inc  rdi
    jmp  .counter
  .end:
    sub  rdi, rax
    mov  rax, rdi
    ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi
    call string_length
    mov  rdx, rax
    pop rsi
    mov  rax, 1
    mov  rdi, 1
    syscall
    ret

; Принимает код символа и выводит его в stdout
print_char:
    push rdi
    mov rsi, rsp
    mov rdi, 1
    mov rdx, 1
    mov rax, 1
    syscall
    pop rdi
    ret


; Переводит строку (выводит символ с кодом 0xA)
print_newline:
    mov rdi, 10
    jmp print_char


; Выводит беззнаковое 8-байтовое число в десятичном формате
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    xor rax, rax
    xor rdx, rdx
    xor rsi, rsi

    push rbx
    mov rbx, rsp
    mov rax, rdi
    mov rsi, 10
    mov [rsp], byte 0

    .loop:
        div rsi

        lea rdx, ['0' + rdx]
        dec rsp
        mov [rsp], dl
        xor rdx, rdx
        test rax, rax
        jnz .loop

    mov rdi, rsp
    call print_string

    mov rsp, rbx
    pop rbx
    ret

; Выводит знаковое 8-байтовое число в десятичном формате
print_int:
    mov rax, rdi
    test rax, rax
    jns .print_uint
    push rdi
    mov rdi, '-'
    call print_char
    pop rdi
    neg rdi

    .print_uint:
        jmp print_uint

; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    xor rax, rax
    xor rcx, rcx

    .loop:
        mov al, byte [rdi + rcx]
        cmp al, byte [rsi + rcx]
        jne .ne
        cmp byte [rdi + rcx], 0
        je .eq
        cmp byte [rsi + rcx], 0
        je .eq
        inc rcx
        jmp .loop

    .ne:
        mov rax, 0
        ret

    .eq:
        mov rax, 1
        ret


; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    sub rsp, 32      ; Allocate space for a character
    mov rdi, 0
    mov rsi, rsp
    mov rdx, 1
    mov rax, 0
    syscall
    cmp rax, 0
    je .eof
    movzx rax, byte [rsp]
    add rsp, 32      ; Restore the stack
    ret
.eof:
    add rsp, 32      ; Restore the stack
    xor rax, rax
    ret

; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале, .
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax
; Эта функция должна дописывать к слову нуль-терминатор
read_word:
    push r12
    push r13
    push r14
    mov r12, rdi
    mov r13, rsi
    xor r14, r14

.loop:
    call read_char
    test rax, rax
    jz .done
    cmp r14, r13
    jnl .err
    cmp rax, 0x20
    jz .skip
    cmp rax, 0x9
    jz .skip
    cmp rax, 0xA
    jz .skip
    mov [r12 + r14], al
    inc r14
    jmp .loop

.skip:
    test r14, r14
    jz .loop

.done:
    mov byte[r12 + r14], 0
    mov rax, r12
    mov rdx, r14
    jmp .exit

.err:
    xor rax, rax

.exit:
    pop r14
    pop r13
    pop r12
    ret


; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    xor rax, rax
    xor rdx, rdx

    .read_loop:
        movzx rcx, byte [rdi + rdx]

        test rcx, rcx
        jz .done

        cmp rcx, '0'
        jb .not_a_digit
        cmp rcx, '9'
        ja .not_a_digit

        sub rcx, '0'

        imul rax, rax, 10
        add  rax, rcx

        inc  rdx
        jmp  .read_loop

    .not_a_digit:
        test rax, rax
        jnz .done
        xor rdx, rdx

    .done:
        ret


; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был)
; rdx = 0 если число прочитать не удалось
; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был)
; rdx = 0 если число прочитать не удалось
parse_int:
    xor rsi, rsi
    cmp byte[rdi], '-'
    je .negative
    cmp byte[rdi], '+'
    je .positive

    .unsigned:
        jmp parse_uint

    .negative:
        inc rdi
        call parse_uint

    inc rdx
    neg rax
    ret

    .positive:
        inc rdi

    jmp parse_uint


; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    xor rax, rax
    .loop:
            cmp rax, rdx
            jz .return_zero
            mov r9b, [rdi + rax]
            mov [rsi + rax], r9b
            inc rax
            test r9b, r9b
            jz .return
            jmp .loop

    .return_zero:
            xor rax, rax

    .return:
            ret

print_error:
    push rdi
    call string_length
    mov  rdx, rax
    pop  rsi
    mov  rax, 1
    mov  rdi, 2
    syscall
    call print_newline
    ret

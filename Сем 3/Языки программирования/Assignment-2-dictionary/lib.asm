section .text

%define SYS_EXIT 60
%define SYS_WRITE 1
%define SYS_READ  0
%define STDOUT 1
%define STDIN  0
%define STDERR 2


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
global read_line

; Принимает код возврата и завершает текущий процесс
exit:
    mov rax, SYS_EXIT
    syscall

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    xor rax, rax
  .loop:
    cmp   byte[rax+rdi], 0
    je    .end
    inc   rax
    jmp   .loop
  .end:
    ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi
    call string_length
    mov  rdx, rax
    pop rsi
    mov  rax, SYS_WRITE
    mov  rdi, STDOUT
    syscall
    ret

; Принимает код символа и выводит его в stdout
print_char:
    push rdi
    mov rsi, rsp
    mov rax, SYS_WRITE
    mov rdi, STDIN
    mov rdx, 1
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
    push rbp
    mov rbp, rsp
    mov r10, 10
    mov rax, rdi
    mov r8, rsp
    sub rsp, 64
    dec r8
    mov byte[r8], 0

  .loop:
    xor rdx, rdx
    div r10
    add dl, '0'
    dec r8
    mov [r8], dl
    test rax, rax
    jnz .loop
    mov rdi, r8
    call print_string
    mov r8, rbp
    add rsp, 64
    pop rbp
    ret


; Выводит знаковое 8-байтовое число в десятичном формате
print_int:
    test rdi, rdi
    jge .greater
    push rdi
    mov rdi, '-'
    call print_char
    pop rdi
    neg rdi
    call print_uint
    ret

  .greater:
    call print_uint
    ret



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
    mov rax, SYS_READ
    mov rdi, STDIN
    mov rdx, 1
    dec rsp
    mov rsi, rsp
    syscall
    test rax, rax
    jz .quit_read
    mov al, [rsp]
  .quit_read:
    inc rsp
    ret


; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале.
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
    cmp   byte[rdi], '-'
    je    .signed
    cmp   byte[rdi], '+'
    je    .unsigned
    jmp   parse_uint

  .unsigned:
    inc   rdi
    call  parse_uint
    inc   rdx
    ret

  .signed:
    inc   rdi
    call  parse_uint
    neg   rax
    inc   rdx
    ret



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
    mov  rax, SYS_WRITE
    mov  rdi, STDERR
    syscall
    call print_newline
    ret

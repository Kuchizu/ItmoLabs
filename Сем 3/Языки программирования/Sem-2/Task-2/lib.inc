section .text
 
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

; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    xor rax, rax
    ret

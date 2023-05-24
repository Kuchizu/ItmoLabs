ORG 0x0
V0: WORD $default,  0X180
V1: WORD $default,  0X180
V2: WORD $int2,     0X180
V3: WORD $int1,     0X180
V4: WORD $default,  0X180
V5: WORD $default,  0X180
V6: WORD $default,  0X180
V7: WORD $default,  0X180

ORG 0x045
X: WORD ?

max: WORD 0x0018    ; 24,  максимальное значение Х
min: WORD 0xFFE6    ; -26, минимальное значение Х

default: IRET       ; Обработка прерывания по умолчанию

START: DI
    CLA

    LD #0xB         ; Загрузка в аккумулятор MR (1000|0001=1001)
    OUT 0x7         ; Разрешение прерываний для 3 ВУ

    LD #0xA         ; Загрузка в аккумулятор MR (1000|0010=1010)
    OUT 0x5         ; Разрешение прерываний для 2 ВУ
    EI

main: DI
    LD X
    DEC
    CALL check
    ST X
    EI
    JUMP main

int1: DI            ; Обработка прерывания на ВУ-3
    LD X            ; Загрузить X в аккумулятор
    NEG             ; Инвертировать знак X
    ASL             ; Удвоить X (эквивалент умножению на 2)
    ASL             ; Удвоить X (эквивалент умножению на 4)
    SUB X           ; Вычесть X (эквивалент умножению на 5)
    SUB #6          ; Вычесть 6
    CALL check
    OUT 0x6         ; вывод результата на ВУ-3
    EI
    IRET

int2: DI            ; Обработка прерывания на ВУ-2
    IN 0x4          ; чтение содержимого РД ВУ-2
    ADD X           ; прибавление к нему X
    CALL check
    ST X            ; сохранение результата в X
    EI
    IRET

check:                      ; Проверка принадлежности X к ОДЗ
check_min:  CMP min         ; Если x > min переход на проверку верхней границы
            BPL check_max   
            JUMP ld_min     ; Иначе загрузка min в аккумулятор
check_max:  CMP max         ; Проверка пересечения верхней границы X
            BMI return      ; Если x < max переход
ld_min:     LD max          ; Загрузка минимального значения в X 

return: RET
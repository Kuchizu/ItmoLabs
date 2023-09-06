; hello.asm
  section .data
  first: db  'First message', 10
  second: db 'Second message', 10

  section .text
  global _start

  _start:
      mov     rax, 1           ; 'write' syscall number
      mov     rdi, 1           ; stdout descriptor
      mov     rsi, first       ; string address
      mov     rdx, 14          ; string length in bytes
      syscall

      mov     rax, 1           ; 'write' syscall number
      mov     rdi, 2           ; stdout descriptor
      mov     rsi, second      ; string address
      mov     rdx, 14          ; string length in bytes
      syscall

      mov     rax, 60          ; 'exit' syscall number
      xor     rdi, rdi
      syscall

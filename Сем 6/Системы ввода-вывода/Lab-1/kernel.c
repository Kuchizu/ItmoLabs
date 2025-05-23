#include "common.h"

extern char __bss[], __bss_end[], __stack_top[];

struct sbiret{
    long error;
    long value;
};

struct sbiret sbi_call(long arg0, long arg1, long arg2, long arg3, long arg4, long arg5, long fid, long eid) {
    register long a0 __asm__ ("a0") = arg0;
    register long a1 __asm__ ("a1") = arg1;
    register long a2 __asm__ ("a2") = arg2;
    register long a3 __asm__ ("a3") = arg3;
    register long a4 __asm__ ("a4") = arg4;
    register long a5 __asm__ ("a5") = arg5;
    register long a6 __asm__ ("a6") = fid;
    register long a7 __asm__ ("a7") = eid;

    __asm__ volatile (
        "ecall"
        : "=r" (a0), "=r" (a1)
        : "r" (a0), "r" (a1), "r" (a2), "r" (a3), "r" (a4), "r" (a5), "r" (a6), "r" (a7)
        : "memory"
    );

    return (struct sbiret) { .error = a0, .value = a1 };
}

void putchar(char ch) {
    sbi_call(ch, 0, 0, 0, 0, 0, 0, 0x1 /* Console Putchar */);
}


long getchar(void) {
    struct sbiret ret = sbi_call(0, 0, 0, 0, 0, 0, 0, 0x02);
    return ret.error;
}

void puts(const char *s) {
    while (*s) {
        putchar(*s++);
    }
}

void kernel_main(void) {
    puts("Hello, world!");
    
    int input;
    puts("\n");
    printf("1. Get SBI specification version\n");
    printf("2. Get number of counters\n");
    printf("3. Get details of a counter\n");
    printf("4. System Shutdown\n");
    for (;;) {
        input = getchar();
        if (input < 0){
            continue;
        }
        putchar((char)input);
        putchar('\n');

        switch (input) {
        case '1': {
            struct sbiret ans = sbi_call(0, 0, 0, 0, 0, 0, 0, 0x10);
            int minor = ans.value & 0xFFFFFF;
            int major = (ans.value >> 24) & 0x7F;
            printf("SBI specification version: %d.%d\n", major, minor);
            break;
        }
        case '2': {
            puts("Number of counters: ");

            struct sbiret res = sbi_call(0, 0, 0, 0, 0, 0, 0, 0x504D55);

            printf("%d", res.value);
            puts("\n");
            break;
        }
        case '3': {
            puts("Details of counter: ");
            int counter_number1;
            while ((counter_number1 = getchar()) == -1);
            putchar(counter_number1);
            counter_number1 -= 48;
            int counter_number2;
            while ((counter_number2 = getchar()) == -1);
            putchar(counter_number2);
            counter_number2 -= 48;

            int counter_number = 10*counter_number1 + counter_number2;
            puts("\n");

            struct sbiret res = sbi_call(counter_number, 0, 0, 0, 0, 0, 1, 0x504D55);

            int type = (res.value >> 31) & 0x1;
            if (type == 0){
                printf("Type: Hardware\n");
                int width = (res.value >> 12) & 0x3F;
                printf("Width = %d\n", width);
                int csr = res.value & 0xFFF;
                printf("CSR = %d\n", csr);
            } else {
                printf("Type: Firmware\n");
            }
        
            puts("\n");
            break;
        }
        case '4': {
            puts("System shutdown...");
            sbi_call(0, 0, 0, 0, 0, 0, 0, 0x08);
            break;
        }
        default:
            puts("\nInvalid input\n");
        }
        }
    }
__attribute__((section(".text.boot")))
__attribute__((naked))
void boot(void) {
    __asm__ __volatile__(
        "mv sp, %[stack_top]\n" // Set the stack pointer
        "j kernel_main\n"       // Jump to the kernel main function
        :
        : [stack_top] "r" (__stack_top) // Pass the stack top address as %[stack_top]
    );
}

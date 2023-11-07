extern int A, B, R;
extern void process(void);

#include <stdio.h>

int main() {
    A = 5;
    B = 6;

    process();

    printf("Result: %d\n", R);
    return 0;
}

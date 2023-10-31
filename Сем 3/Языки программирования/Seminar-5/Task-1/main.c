#include <stdio.h>

#define print_var(x) printf(#x " is %d", x )

#define MY_CONST 42

int main() {
    int variable = 10;
    print_var(variable);
    print_var(5);
    print_var(MY_CONST);
    return 0;
}

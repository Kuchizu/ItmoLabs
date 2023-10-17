#include <stdio.h>
#include <string.h>
#include <stddef.h>

extern void print_file(const char*);
extern void print_string(const char*);

int main() {
    char filename[256];
    print_string("Please enter file name: ");
    scanf("%255s", filename);
    print_file(filename);

    return 0;
}

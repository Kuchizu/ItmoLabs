#include <stdio.h>
#include <string.h>
#include <stddef.h>

extern void print_file(const char*);
extern void print_string(const char*);

int main() {
    char filename[256];
    print_string("Please enter file name: ");
    fgets(filename, sizeof(filename), stdin);

    size_t len = strlen(filename);
    if (len > 0 && filename[len - 1] == '\n'){
        filename[len - 1] = '\0';
    }
    print_file(filename);

    return 0;
}

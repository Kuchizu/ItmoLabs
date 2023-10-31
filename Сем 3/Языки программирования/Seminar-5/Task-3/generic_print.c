#include <stdio.h>

#define list_push(list, item) _Generic((list), \
    int*: list_push_int, \
    float*: list_push_float, \
    char*: list_push_char \
)(list, item)

#define list_print(list) _Generic((list), \
    int*: list_print_int, \
    float*: list_print_float, \
    char*: list_print_char \
)(list)

void list_push_int(int* list, int item) {
    list[list[0] + 1] = item;
    list[0]++;
}

void list_print_int(int* list) {
    int length = list[0];
    for (int i = 1; i <= length; i++) {
        printf("%d ", list[i]);
    }
    printf("\n");
}

void list_push_float(float* list, float item) {
    list[(int)list[0] + 1] = item;
    list[0]++;
}

void list_print_float(float* list) {
    int length = (int)list[0];
    for (int i = 1; i <= length; i++) {
        printf("%.2f ", list[i]);
    }
    printf("\n");
}

void list_push_char(char* list, char item) {
    list[list[0] + 1] = item;
    list[0]++;
}

void list_print_char(char* list) {
    int length = list[0];
    for (int i = 1; i <= length; i++) {
        printf("%c ", list[i]);
    }
    printf("\n");
}

int main() {
    int intList[100] = {0};
    list_push(intList, 10);
    list_push(intList, 20);
    list_push(intList, 30);
    list_print(intList);

    float floatList[100] = {0};
    list_push(floatList, 3.14);
    list_push(floatList, 2.718);
    list_print(floatList);

    char charList[100] = {0};
    list_push(charList, 'A');
    list_push(charList, 'B');
    list_push(charList, 'C');
    list_push(charList, 'D');
    list_print(charList);

    return 0;
}
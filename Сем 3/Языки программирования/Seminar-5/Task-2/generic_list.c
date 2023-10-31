#include <stdio.h>
#include <stdlib.h>

#define PRINT_SPECIFIER(type) _Generic((type), \
    int64_t: "%lld",                         \
    double: "%f",                            \
    default: "%lld")

#define DEFINE_LIST(type)                                    \
  struct list_##type {                                       \
    type value;                                              \
    struct list_##type* next;                                \
  };                                                         \
  void list_##type##_push(struct list_##type** head, type value) { \
    struct list_##type* new_node = malloc(sizeof(struct list_##type)); \
    new_node->value = value;                                 \
    new_node->next = NULL;                                   \
    if (*head == NULL) {                                     \
      *head = new_node;                                      \
    } else {                                                 \
      struct list_##type* temp = *head;                      \
      while (temp->next) {                                   \
        temp = temp->next;                                   \
      }                                                      \
      temp->next = new_node;                                 \
    }                                                        \
  }                                                          \
  void list_##type##_print(struct list_##type* head) {       \
    while (head) {                                           \
      printf(PRINT_SPECIFIER(head->value), head->value);     \
      printf(" ");                                           \
      head = head->next;                                     \
    }                                                        \
    printf("\n");                                            \
  }

DEFINE_LIST(int64_t)
DEFINE_LIST(double)

int main() {
    struct list_int64_t* int_list = NULL;
    list_int64_t_push(&int_list, 1);
    list_int64_t_push(&int_list, 0);
    list_int64_t_print(int_list);

    struct list_double* double_list = NULL;
    list_double_push(&double_list, 1.32152233);
    list_double_push(&double_list, 2.5);
    list_double_push(&double_list, 3.5);
    list_double_push(&double_list, 3.5);
    list_double_print(double_list);

    return 0;
}

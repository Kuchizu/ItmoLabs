#include "vector.h"
#include <stdint.h>
#include <inttypes.h>

void print_element(int64_t element, FILE* stream) {
    fprintf(stream, "%" PRId64 " ", element);
}

int main() {
    vector_t* vec = vector_create(5);
    for (int64_t i = 0; i <= 10; ++i) {
        vector_push_back(vec, i * i);
    }

    int64_t somearr[] = {1, 2, 3, 4, 5};
    vector_append(vec, somearr, sizeof(somearr) / sizeof(somearr[0]));

    vector_foreach(vec, print_element, stdout);
    vector_free(vec);

    printf("\n");

    return 0;
}


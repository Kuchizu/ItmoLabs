#include "vector.h"
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>

struct vector {
    int64_t* array;
    size_t capacity;
    size_t count;
};

vector_t* vector_create(size_t initial_capacity) {
    vector_t* vec = malloc(sizeof(vector_t));
    if (vec) {
        vec->array = malloc(sizeof(int64_t) * initial_capacity);
        vec->capacity = initial_capacity;
        vec->count = 0;
    }
    return vec;
}

void vector_free(vector_t* vec) {
    if (vec) {
        free(vec->array);
        free(vec);
    }
}

size_t vector_length(const vector_t* vec) {
    return vec ? vec->count : 0;
}

size_t vector_capacity(const vector_t* vec) {
    return vec ? vec->capacity : 0;
}

int64_t vector_get(const vector_t* vec, size_t index) {
    if (vec && index < vec->count) {
        return vec->array[index];
    }
    return 0;
}

void vector_set(vector_t* vec, size_t index, int64_t value) {
    if (vec && index < vec->count) {
        vec->array[index] = value;
    }
}

void vector_push_back(vector_t* vec, int64_t value) {
    if (!vec || vec->count == vec->capacity) {
        size_t new_capacity = vec->capacity ? vec->capacity * 2 : 1;
        int64_t* new_array = realloc(vec->array, sizeof(int64_t) * new_capacity);
        if (new_array) {
            vec->array = new_array;
            vec->capacity = new_capacity;
        } else {
            return; // Unk err
        }
    }
    vec->array[vec->count++] = value;
}

// Добавление другого массива в конец вектора
void vector_append(vector_t* vec, const int64_t* values, size_t count) {
    if (!vec || !values) return;

    while (vec->count + count > vec->capacity) {
        size_t new_capacity = vec->capacity ? vec->capacity * 2 : 1;
        int64_t* new_array = realloc(vec->array, sizeof(int64_t) * new_capacity);
        if (!new_array) return; // Ошибка выделения памяти
        vec->array = new_array;
        vec->capacity = new_capacity;
    }

    memcpy(vec->array + vec->count, values, sizeof(int64_t) * count);
    vec->count += count;
}

// Изменение размера массива
void vector_resize(vector_t* vec, size_t new_size) {
    if (!vec) return;
    if (new_size > vec->capacity) {
        int64_t* new_array = realloc(vec->array, sizeof(int64_t) * new_size);
        if (!new_array) return; // Ошибка выделения памяти
        vec->array = new_array;
        vec->capacity = new_size;
    }
    if (new_size < vec->count) {
        vec->count = new_size;
    }
}

// Вывод содержимого вектора в поток
void vector_print(const vector_t* vec, FILE* stream) {
    if (!vec || !stream) return;
    for (size_t i = 0; i < vec->count; ++i) {
        fprintf(stream, "%" PRId64 " ", vec->array[i]);
    }
}

// Применение функции к каждому элементу вектора
void vector_foreach(const vector_t* vec, void (*func)(int64_t, FILE*), FILE* stream) {
    if (!vec || !func) return;
    for (size_t i = 0; i < vec->count; ++i) {
        func(vec->array[i], stream);
    }
}


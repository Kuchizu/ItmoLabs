#ifndef VECTOR_H
#define VECTOR_H

#include <stddef.h>
#include <stdio.h>
#include <stdint.h>

// Определение структуры вектора
typedef struct vector vector_t;

// Функции для работы с вектором
vector_t* vector_create(size_t initial_capacity);
void vector_free(vector_t* vec);
size_t vector_length(const vector_t* vec);
size_t vector_capacity(const vector_t* vec);
int64_t vector_get(const vector_t* vec, size_t index);
void vector_set(vector_t* vec, size_t index, int64_t value);
void vector_push_back(vector_t* vec, int64_t value);
void vector_append(vector_t* vec, const int64_t* values, size_t count);
void vector_resize(vector_t* vec, size_t new_size);

// Функции для вывода
void vector_print(const vector_t* vec, FILE* stream);
void vector_foreach(const vector_t* vec, void (*func)(int64_t, FILE*), FILE* stream);

#endif // VECTOR_H

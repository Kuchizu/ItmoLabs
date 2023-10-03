#ifndef HEADERS_H
#define HEADERS_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void create_file(const char *filename);
void delete_file(const char *filename);
void rename_file(const char *old_name, const char *new_name);
void list_files(void);

#endif

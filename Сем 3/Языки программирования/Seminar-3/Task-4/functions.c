#include "headers.h"
#include <dirent.h>

void create_file(const char *filename) {
    FILE *file = fopen(filename, "w");
    if (file != NULL) {
        printf("File %s created successfully.\n", filename);
        fclose(file);
    } else {
        perror("Error");
    }
}

void delete_file(const char *filename) {
    if (remove(filename) == 0) {
        printf("File %s deleted successfully.\n", filename);
    } else {
        perror("Error");
    }
}

void rename_file(const char *old_name, const char *new_name) {
    if (rename(old_name, new_name) == 0) {
        printf("File %s renamed to %s successfully.\n", old_name, new_name);
    } else {
        perror("Error");
    }
}

void list_files(void) {
    DIR *d;
    struct dirent *dir;
    d = opendir(".");
    if (d) {
        while ((dir = readdir(d)) != NULL) {
            printf("%s\n", dir->d_name);
        }
        closedir(d);
    } else {
        perror("Error");
    }
}

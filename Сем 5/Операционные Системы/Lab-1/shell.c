#define _GNU_SOURCE
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/wait.h>

#define BUFFER_SIZE 1024
#define STACK_SIZE (1024 * 1024)    // Stack size for child process

static int child_func(void *arg) {
    char **args = (char **)arg;
    if (execvp(args[0], args) == -1) {
        perror("Execution failed");
        exit(EXIT_FAILURE);
    }
    return 0; // Never reaches here
}

int main() {
    char command_line[BUFFER_SIZE];

    while (1) {
        printf("shell> ");
        if (fgets(command_line, BUFFER_SIZE, stdin) == NULL) {
            printf("\n");
            break;
        }

        // Strip
        size_t length = strlen(command_line);
        if (command_line[length - 1] == '\n') {
            command_line[length - 1] = '\0';
        }

        if (strlen(command_line) == 0) {
            continue;
        }

        char *args[64];
        int arg_count = 0;
        char *token = strtok(command_line, " \t");
        while (token != NULL) {
            args[arg_count++] = token;
            token = strtok(NULL, " \t");
        }
        args[arg_count] = NULL;

        if (strcmp(args[0], "exit") == 0) {
            break;
        }

        struct timeval start, end;
        gettimeofday(&start, NULL);

        // Malloc memory for child process
        void *child_stack = malloc(STACK_SIZE);
        if (child_stack == NULL) {
            perror("Failed to allocate memory for child stack");
            continue;
        }

        // Making child process using clone
        pid_t pid = clone(child_func, child_stack + STACK_SIZE, SIGCHLD, args);
        if (pid == -1) {
            perror("Clone failed");
            free(child_stack);
            continue;
        }

        int status;
        if (waitpid(pid, &status, 0) == -1) {
            perror("Waitpid failed");
            free(child_stack);
            continue;
        }

        gettimeofday(&end, NULL);
        long elapsed = (end.tv_sec - start.tv_sec) * 1000;
        elapsed += (end.tv_usec - start.tv_usec) / 1000;
        printf("Execution time: %ld ms\n", elapsed);

        free(child_stack);
    }

    return 0;
}

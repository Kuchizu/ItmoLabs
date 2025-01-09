#define _GNU_SOURCE
#include <dirent.h>
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <unistd.h>

#define BUFFER_SIZE 1024
#define STACK_SIZE (1024 * 1024)  // Stack size for child process

void execute_ls(void) {
  DIR* dir = opendir(".");
  if (dir == NULL) {
    perror("Failed to open directory");
    return;
  }
  struct dirent* entry;
  while ((entry = readdir(dir)) != NULL) {
    printf("%s\n", entry->d_name);
  }
  closedir(dir);
}

void execute_pwd(void) {
  char cwd[BUFFER_SIZE];
  if (getcwd(cwd, sizeof(cwd)) == NULL) {
    perror("Failed to get current working directory");
  } else {
    printf("%s\n", cwd);
  }
}

void execute_cd(char* path) {
  if (chdir(path) != 0) {
    perror("Failed to change directory");
  }
}

static int child_func(void* arg) {
  char** args = (char**)arg;
  if (execvp(args[0], args) == -1) {
    perror("Execution failed");
    exit(EXIT_FAILURE);
  }
  return 0;  // Never reaches here
}

int main(void) {
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

    char* args[64];
    int arg_count = 0;
    char* token = strtok(command_line, " \t");
    while (token != NULL) {
      args[arg_count++] = token;
      token = strtok(NULL, " \t");
    }
    args[arg_count] = NULL;

    if (strcmp(args[0], "exit") == 0) {
      break;
    } else if (strcmp(args[0], "ls") == 0) {
      execute_ls();
      continue;
    } else if (strcmp(args[0], "pwd") == 0) {
      execute_pwd();
      continue;
    } else if (strcmp(args[0], "cd") == 0) {
      if (arg_count < 2) {
        fprintf(stderr, "cd: missing argument\n");
      } else {
        execute_cd(args[1]);
      }
      continue;
    }

    struct timeval start, end;
    gettimeofday(&start, NULL);

    // Malloc memory for child process
    char* child_stack = malloc(STACK_SIZE);
    if (child_stack == NULL) {
      perror("Failed to allocate memory for child stack");
      continue;
    }

    // Clone --> Child process
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

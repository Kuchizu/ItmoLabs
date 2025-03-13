#ifndef ELF_UTILS_H
#define ELF_UTILS_H

#include <elf.h>
#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdint.h>
#include <sys/mman.h>
#include <unistd.h>

#define PAGE_SIZE 0x1000

typedef void (*function_pointer)(void);

int read_headers(int file_descriptor, Elf64_Phdr *segment_headers, Elf64_Ehdr *elf_header);
uint8_t convert_prot_flags(uint32_t flags);
int load_segment(int file_descriptor, Elf64_Phdr *segment_header);
int compare_section_names(int file_descriptor, Elf64_Word name_offset, Elf64_Shdr *section_name_table, const char *target_name);
bool is_executable_segment(Elf64_Shdr *section_header);
int find_and_execute_section(int file_descriptor, Elf64_Ehdr *elf_header, const char *target_section_name);

#endif // ELF

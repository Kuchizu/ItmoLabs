#include "include/elf.h"

int read_headers(int file_descriptor, Elf64_Phdr *segment_headers, Elf64_Ehdr *elf_header) {
    if (lseek(file_descriptor, (off_t)elf_header->e_phoff, SEEK_SET) == -1) {
        return EIO;
    }
    for (int idx = 0; idx < elf_header->e_phnum; idx++) {
        if (read(file_descriptor, &segment_headers[idx], sizeof(Elf64_Phdr)) != sizeof(Elf64_Phdr)) {
            return EIO;
        }
    }
    return 0;
}

uint8_t convert_prot_flags(uint32_t flags) {
    uint8_t protection_flags = 0;

    if (flags & PF_R) {
        protection_flags |= PROT_READ;
    }

    if (flags & PF_W) {
        protection_flags |= PROT_WRITE;
    }

    if (flags & PF_X) {
        protection_flags |= PROT_EXEC;
    }

    return protection_flags;
}

int load_segment(int file_descriptor, Elf64_Phdr *segment_header) {
    if (segment_header->p_type == PT_LOAD) {
        Elf64_Addr aligned_virtual_addr = segment_header->p_vaddr;
        Elf64_Xword aligned_memory_size = segment_header->p_memsz;

        if (aligned_virtual_addr % PAGE_SIZE != 0) {
            aligned_memory_size += aligned_virtual_addr % PAGE_SIZE;
            aligned_virtual_addr -= aligned_virtual_addr % PAGE_SIZE;
        }

        void *mapped_addr = (void *)(uintptr_t)aligned_virtual_addr; // NOLINT
        if (mmap(mapped_addr, aligned_memory_size, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_FIXED | MAP_PRIVATE | MAP_ANONYMOUS, -1, 0) == MAP_FAILED) {
            return EIO;
        }

        if (lseek(file_descriptor, (off_t)segment_header->p_offset, SEEK_SET) == -1) {
            return EIO;
        }

        void *segment_addr = (void *)segment_header->p_vaddr; // NOLINT
        if (read(file_descriptor, segment_addr, segment_header->p_filesz) != segment_header->p_filesz) {
            return EIO;
        }

        uint8_t protection_flags = convert_prot_flags(segment_header->p_flags);
        if (mprotect(mapped_addr, aligned_memory_size, protection_flags) == -1) {
            return EIO;
        }
    }
    return 0;
}


int compare_section_names(int file_descriptor, Elf64_Word name_offset, Elf64_Shdr *section_name_table, const char *target_name) {
    if (lseek(file_descriptor, (off_t)section_name_table->sh_offset + name_offset, SEEK_SET) == -1) {
        return EIO;
    }
    char char_buffer;
    Elf64_Word index = 0;

    while (true) {
        if (read(file_descriptor, &char_buffer, 1) != 1) {
            return EIO;
        }

        if (target_name[index] == '\0') {
            return (char_buffer == '\0') ? 1 : 0;
        }

        if (char_buffer != target_name[index]) {
            return 0;
        }

        index++;
    }
}

bool is_executable_segment(Elf64_Shdr *section_header) {
    return section_header->sh_flags & SHF_EXECINSTR;
}

int find_and_execute_section(int file_descriptor, Elf64_Ehdr *elf_header, const char *target_section_name) {
    if (lseek(file_descriptor, (off_t)elf_header->e_shoff, SEEK_SET) == (off_t)-1) {
        return EIO;
    }

    Elf64_Shdr section_headers[elf_header->e_shnum];
    if (read(file_descriptor, section_headers, sizeof(Elf64_Shdr) * elf_header->e_shnum) != sizeof(Elf64_Shdr) * elf_header->e_shnum) {
        return EIO;
    }

    Elf64_Shdr section_name_table = section_headers[elf_header->e_shstrndx];

    for (size_t idx = 0; idx < elf_header->e_shnum; idx++) {
        if (compare_section_names(file_descriptor, section_headers[idx].sh_name, &section_name_table, target_section_name) == 1) {
            if (is_executable_segment(&section_headers[idx])) {
                function_pointer start_function = (function_pointer)section_headers[idx].sh_addr; // NOLINT
                start_function();
                return 0;

            } else {
                return EINVAL;
            }
        }
    }
    return EINVAL;
}

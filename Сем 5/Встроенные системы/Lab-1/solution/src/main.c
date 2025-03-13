#include "include/elf.h"

int main(int argc, char **argv) {
    if (argc != 3) {
        return EINVAL;
    }

    int file_descriptor = open(argv[1], O_RDONLY);
    if (file_descriptor == -1) {
        return ENOENT;
    }

    Elf64_Ehdr elf_header;
    if (read(file_descriptor, &elf_header, sizeof(Elf64_Ehdr)) != sizeof(Elf64_Ehdr)) {
        close(file_descriptor);
        return EIO;
    }

    if (elf_header.e_ident[EI_MAG0] != ELFMAG0 ||
        elf_header.e_ident[EI_MAG1] != ELFMAG1 ||
        elf_header.e_ident[EI_MAG2] != ELFMAG2 ||
        elf_header.e_ident[EI_MAG3] != ELFMAG3) {
        close(file_descriptor);
        return ENOEXEC;
    }

    Elf64_Off ph_offset = elf_header.e_phoff;

    if (lseek(file_descriptor, (off_t)ph_offset, SEEK_SET) == (off_t)-1) {
        close(file_descriptor);
        return EIO;
    }

    Elf64_Phdr segment_headers[elf_header.e_phnum];
    if (read_headers(file_descriptor, segment_headers, &elf_header) != 0) {
        close(file_descriptor);
        return EIO;
    }

    for (int idx = 0; idx < elf_header.e_phnum; idx++) {
        if (load_segment(file_descriptor, &segment_headers[idx]) != 0) {
            close(file_descriptor);
            return EIO;
        }
    }

    if (find_and_execute_section(file_descriptor, &elf_header, argv[2]) != 0) {
        close(file_descriptor);
        return EINVAL;
    }
    close(file_descriptor);

    return 0;
}

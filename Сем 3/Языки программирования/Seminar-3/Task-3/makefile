program: lib.o main.o
	ld main.o lib.o -o program
	rm ./*.o

lib.o: lib.asm
	nasm -f elf64 lib.asm -o lib.o

main.o: main.asm
	nasm -f elf64 main.asm -o main.o

# .PHONY:	clean
# clean:
# 	rm ./*.o

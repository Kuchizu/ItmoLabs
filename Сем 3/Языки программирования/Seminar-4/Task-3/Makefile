all: program

program: hello.o string.o
	gcc hello.o string.o -o program

hello.o: hello.c
	gcc -c hello.c -o hello.o

string.o: string.asm
	nasm -f elf64 string.asm -o string.o

clean:
	rm -f *.o program

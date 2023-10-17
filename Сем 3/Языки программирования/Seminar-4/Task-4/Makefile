TARGET = program

OBJS = main.o print_file.o

all: $(TARGET)

$(TARGET): $(OBJS)
	gcc $(OBJS) -o $(TARGET)

main.o: main.c
	gcc -c main.c -o main.o

print_file.o: print_file.asm
	nasm -f elf64 print_file.asm -o print_file.o

clean:
	rm -f $(OBJS) $(TARGET)

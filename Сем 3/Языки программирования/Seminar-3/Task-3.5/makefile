NASM = nasm
ELFFLAG = -f elf64
LD = ld
OBJECTS = lib.o main.o
TARGET = program

%.o: %.asm
	$(NASM) $(ELFFLAG) $< -o $@

$(TARGET): $(OBJECTS)
	$(LD) $^ -o $@

.PHONY: clean
clean:
	rm -f ./*.o $(TARGET)

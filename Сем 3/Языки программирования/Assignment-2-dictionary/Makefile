ASM=nasm
ASMFLAGS=-f elf64
PYTHON=python3

%.o: %.asm
	$(ASM) $(ASMFLAGS) -o $@ $<

program: lib.o dict.o main.o
	ld -o program lib.o dict.o main.o

words.inc: colon.inc
main.asm: words.inc

.PHONY: clean
clean:
	rm -f *.o program

.PHONY: test
test: program
	$(PYTHON) test.py

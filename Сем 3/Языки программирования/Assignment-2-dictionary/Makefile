ASM = nasm 
ASMFLAG = -f elf64
LD = ld
PY = python3

%.o: %.asm
	${ASM} ${ASMFLAG} -o $@ $^


program: lib.o dict.o main.o
	$(LD) -o $@ $^

test:
	$(PY) test.py

clean:
	rm -f *.o program

sum: clean program test

.PHONY: test clean
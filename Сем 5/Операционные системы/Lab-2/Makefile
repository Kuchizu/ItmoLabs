CC = gcc

CFLAGS = -Wall -Wextra -O2 -fPIC

LDFLAGS = -shared

TARGET_LIB = liblab2.so
TEST_APP   = test_app

all: $(TARGET_LIB) $(TEST_APP)

$(TARGET_LIB): lab2.o
	$(CC) $(LDFLAGS) -o $(TARGET_LIB) lab2.o

lab2.o: lab2.c lab2.h
	$(CC) $(CFLAGS) -c lab2.c

$(TEST_APP): main.o $(TARGET_LIB)
	$(CC) -o $(TEST_APP) main.o -L. -llab2

main.o: main.c lab2.h
	$(CC) $(CFLAGS) -c main.c

clean:
	rm -f *.o $(TARGET_LIB) $(TEST_APP)

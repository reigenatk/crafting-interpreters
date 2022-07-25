HEADERS = chunk.h common.h debug.h memory.h value.h vm.h
OBJECTS = chunk.o debug.o memory.o main.o value.o vm.o

default: clox

%.o: %.c $(HEADERS)
	gcc -c $< -o $@

clox: $(OBJECTS)
	gcc $(OBJECTS) -o $@

clean:
	-rm -f $(OBJECTS)
	-rm -f clox

# IDIR =.
# CC=gcc
# CFLAGS=-I$(IDIR)

# ODIR=obj
# # LDIR =../lib

# # LIBS=-lm

# _DEPS = chunk.h common.h debug.h memory.h
# DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

# _OBJ = chunk.o debug.o memory.o main.o
# OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))


# $(ODIR)/%.o: %.c $(DEPS)
# 	$(CC) -c -o $@ $< $(CFLAGS)

# program: $(OBJ)
# 	$(CC) -o $@ $^ $(CFLAGS)

# .PHONY: clean

# clean:
# 	rm -f $(ODIR)/*.o *~ core $(INCDIR)/*~ 
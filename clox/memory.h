#ifndef clox_memory_h
#define clox_memory_h

#include "common.h"

// double the memory space, if its less than 8 bytes just set to 8
#define GROW_CAPACITY(x) \
    ((x) < 8 ? 8 : x * 2)

// this macro will just call reallocate with the appropriate parameters
// also it casts the resulting pointer back to the desired type
#define GROW_ARRAY(type, ptr, oldcapacity, newcapacity) \
    (type*) reallocate(ptr, sizeof(type) * oldcapacity, \
        sizeof(type) * newcapacity)

// this macro calls reallocate with newSize zero, which is the same as calling free()
#define FREE_ARRAY(type, ptr, count, capacity) \
    reallocate(ptr, sizeof(type) * count, 0); 

void* reallocate(void* pointer, size_t oldSize, size_t newSize);


#endif
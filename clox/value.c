#include <stdio.h>
#include "value.h"
#include "memory.h"

// these 3 methods are basically a copy of the ones in chunk.c
void initValueArray(ValueArray* array) {
    array->values = NULL;
    array->capacity = 0;
    array->count = 0;
}

void writeValueArray(ValueArray* array, Value value) {
    if (array->capacity == array->count) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->values = GROW_ARRAY(Value, array->values,
        oldCapacity, array->capacity);
    }
    array->values[array->count] = value;
    array->count++;
}

void freeValueArray(ValueArray* array) {
    FREE_ARRAY(Value, array->values, array->count, array->capacity);
    initValueArray(array);
}

void printValue(Value v) {
    // %g btw means shortest of %e or %f, %e being scientific notation and $f being floating point
    printf("value: %g", v);    
}

#ifndef clox_value_h
#define clox_value_h

#include "common.h"

// represent lox values as doubles in C
typedef double Value;

typedef struct {
    int capacity;
    int count;
    Value* values; // pointer to the values of this valuearray
} ValueArray;

void initValueArray(ValueArray* array);
void writeValueArray(ValueArray* array, Value value);
void freeValueArray(ValueArray* array);
void printValue(Value v);
#endif

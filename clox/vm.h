#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"

typedef struct {
    // the current chunk the VM is executing
    Chunk* chunk; 
    // ip - instruction pointer. the current byte opcode of the current chunk that is executing
    uint8_t* ip; 
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

void initVM();
void freeVM();
InterpretResult interpretChunk(Chunk* c);
InterpretResult run();

#endif
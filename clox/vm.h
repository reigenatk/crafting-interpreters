#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"
#include "value.h"

#define STACK_MAX 256

typedef struct {
    // the current chunk the VM is executing
    Chunk* chunk; 
    // ip - instruction pointer. the current byte opcode of the current chunk that is executing
    uint8_t* ip; 
    // stack of the VM. This holds all the literal values of the program
    // and if we push too many we have a stack overflow
    Value stack[STACK_MAX];
    // The pointer points at the array element just past the element containing the top
    // value on the stack. That seems a little odd, but almost every implementation
    // does this. It means we can indicate that the stack is empty by pointing at
    // element zero in the array
    // another way to think about it is, it points to where the next element should go
    Value* stack_ptr;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

void initVM();
void freeVM();

void resetStack();
void pushStack(Value v);
Value popStack();

InterpretResult interpretChunk(Chunk* c);
#endif
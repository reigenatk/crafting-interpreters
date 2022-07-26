#ifndef clox_chunk_h
#define clox_chunk_h

#include "value.h"
#include "common.h"

typedef enum {
    // takes no arguments, just returns
    OP_RETURN,
    // takes a single byte argument that is the 
    // index into the constants array of which constant to load
    // then puts that value onto the VM stack
    OP_CONSTANT,
    // negate the value at top of stack
    OP_NEGATE,
    // Binary ops
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
} OpCode;

typedef struct {
    // the amount of memory in bytes that is occupied
    int count; 
    // the amount of memory in bytes allocated
    int capacity; 
    // pointer to start of dynamic array
    uint8_t* code;
    // corresponding line number in the source code for each byte in the bytecode 
    // this array will always be in sync with the size and count of code
    int* line_no; 
    // store the constants for this chunk in here 
    ValueArray constants; 
} Chunk;

// set the fields of chunk to zero values
void initChunk(Chunk* chunk);

/**
 * @brief write a byte to this chunk. This allocates memory if it doesn't yet exist for this chunk
 * and also resizes it if there isn't enough memory
 * Also, pass in the line number for this byte so we can do error messages later
 */
void writeChunk(Chunk* c, uint8_t value, int lineNum);

// free the memory associated with this chunk
void freeChunk(Chunk* chunk);

// add a value to the constants of this chunk
int addConstantToChunk(Chunk* chunk, Value v);

#endif
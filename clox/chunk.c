#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* c) {
    c->capacity = 0;
    c->count = 0;
    c->code = NULL;
    c->line_no = NULL;
    initValueArray(&c->constants);
}

void writeChunk(Chunk* c, uint8_t value, int lineNum) {
    // if no room left, expand capacity
    if (c->capacity == c->count) {
        int oldCapacity = c->capacity;
        c->capacity = GROW_CAPACITY(oldCapacity);
        c->code = GROW_ARRAY(uint8_t, c->code, oldCapacity, c->capacity);
        c->line_no = GROW_ARRAY(int, c->line_no, oldCapacity, c->capacity);
    }
    // actually write the value in
    *(c->code + c->count) = value;
    
    // write line number info too
    c->line_no[c->count] = lineNum;
    c->count++;
}

int addConstantToChunk(Chunk* chunk, Value v) {
    writeValueArray(&chunk->constants, v);

    // return the index where the constant was put in the ValueArray for constants
    return chunk->constants.count - 1;
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->count, chunk->capacity);
    FREE_ARRAY(int, chunk->line_no, chunk->count, chunk->capacity);
    initChunk(chunk);
    freeValueArray(&chunk->constants);
}


#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"

// go through the bytecode, and dissasemble back to the instructions
void dissasembleChunk(Chunk* chunk, char* chunkName);

// helper for dissasembleChunk
int dissasembleInstruction(Chunk* chunk, int offset);

#endif
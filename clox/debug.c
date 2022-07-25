#include "debug.h"
#include "value.h"
#include <stdio.h>


void dissasembleChunk(Chunk* chunk, char* chunkName) {
    printf("== %s == \n", chunkName);
    for (int i = 0; i < chunk->count;) {
        // next value of i depends on how much is dissasembled during the call to dissasembleInstruction
        i = dissasembleInstruction(chunk, i);
    }
}

// opcodes that take 0 arguments
static int simpleInstruction(char* name, int offset) {
    printf("%s\n", name);
    return offset + 1;
}

static int constantInstruction(char* name, Chunk* c, int offset) {
    printf("%s ", name);
    uint8_t constantIdx = c->code[offset+1];
    printf("idx: %d ", constantIdx);
    printValue(c->constants.values[constantIdx]);
    printf("\n");
    // + 2 because there are 2 bytes, one opcode and one argument
    return offset + 2;
}

int dissasembleInstruction(Chunk* chunk, int offset) {
    printf("offset: %04d line: %d ", offset, chunk->line_no[offset]);
    uint8_t byte = chunk->code[offset];
    switch(byte) {
        case OP_RETURN:
            return simpleInstruction("OP_RETURN", offset);
        case OP_CONSTANT:
            return constantInstruction("OP_CONSTANT", chunk, offset);
        default:
            printf("Unknown opcode %d\n", byte);
            return offset+1;
    }
}
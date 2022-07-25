#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char* argv[]) {
    // create VM
    initVM();

    // create a chunk with some bytecode in it
    Chunk c;
    initChunk(&c);
    int idx = addConstantToChunk(&c, 21);
    writeChunk(&c, OP_CONSTANT, 1);
    writeChunk(&c, idx, 1);
    writeChunk(&c, OP_RETURN, 2);
    // dissasembleChunk(&c, "test chunk");

    // ask the VM to interpret our bytecode!
    interpretChunk(&c);

    // deallocate everything
    freeVM();
    freeChunk(&c);
    return 0;
}
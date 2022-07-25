#include <stdio.h>
#include "vm.h"


// global instance of VM
VM vm;

void initVM() {

}

void freeVM() {

}


InterpretResult run() {
    // this macro gets the current byte about to be executed then increments the instruction pointer
    #define NEXT_BYTE() *(vm.ip++)
    #define READ_CONSTANT() vm.chunk->constants.values[NEXT_BYTE()]
    // run thru all the bytecode
    for (;;) { 
        uint8_t opcode;
        switch (opcode = NEXT_BYTE()) {
            case OP_RETURN:
                return INTERPRET_OK;
            case OP_CONSTANT:
                Value constant = READ_CONSTANT();
                printValue(constant);
                printf("\n");
                return INTERPRET_OK;
            default:
        }
    }
    #undef NEXT_BYTE
    #undef READ_CONSTANT
}

InterpretResult interpretChunk(Chunk* c) {
    vm.chunk = c;
    vm.ip = vm.chunk->code;
    return run();
}


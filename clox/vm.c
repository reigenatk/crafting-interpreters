#include <stdio.h>
#include "vm.h"
#include "debug.h"
#include "common.h"
#include "value.h"

// global instance of VM
VM vm;

void initVM() {
    resetStack();
}

void freeVM() {

}

void resetStack() {
    vm.stack_ptr = vm.stack;
}

InterpretResult run() {
    // this macro gets the current byte about to be executed then increments the instruction pointer
    #define NEXT_BYTE() *(vm.ip++)
    #define READ_CONSTANT() (vm.chunk->constants.values[NEXT_BYTE()])

    // right operand is popped first!
    #define BINARY_OP(x) \
        do { \
            double right = popStack(); \
            double left = popStack(); \
            pushStack(left x right); \
        } while (false)
        
    // run thru all the bytecode
    for (;;) { 

        // make a macro in common.h that can be toggled which will print out more detailed info about
        // each bytecode instruction that executes
        #ifdef DEBUG_TRACE_EXECUTION
            // show the current contents of the stack
            printf("stack:\t\t");
            for (Value* v = vm.stack; v < vm.stack_ptr; v++) {
                printf("[ ");
                printValue(*v);
                printf(" ]");
            }
            printf("\n");
            dissasembleInstruction(vm.chunk, (int) (vm.ip - vm.chunk->code));
        #endif
        

        uint8_t opcode;
        switch (opcode = NEXT_BYTE()) {
            case OP_RETURN:
                printValue(popStack());
                printf("\n");
                return INTERPRET_OK;
            case OP_NEGATE:
                // just negate the value at top of stack
                pushStack(-popStack());
                break;
            case OP_CONSTANT: {
                Value stuff = READ_CONSTANT();
                printValue(stuff);
                printf("\n");
                pushStack(stuff);
                break;
            }
            case OP_ADD:
                BINARY_OP(+); break;
            case OP_SUBTRACT:
                BINARY_OP(-); break;
            case OP_MULTIPLY:
                BINARY_OP(*); break;  
            case OP_DIVIDE:
                BINARY_OP(/); break;              
            default: break;
        }
    }
    #undef NEXT_BYTE
    #undef READ_CONSTANT
    #undef BINARY_OP
}

InterpretResult interpretChunk(Chunk* c) {
    vm.chunk = c;
    vm.ip = vm.chunk->code;
    return run();
}

void pushStack(Value v) {
    *(vm.stack_ptr++) = v;
}

Value popStack() {
    vm.stack_ptr--;
    return *vm.stack_ptr;
}

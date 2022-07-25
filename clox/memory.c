#include "memory.h"
#include <stdlib.h>

/**
 * @brief 4 cases depending on the oldSize ? newSize comparison
 *  oldSize newSize Operation
    0 Non‑zero                    Allocate new block.
    Non‑zero 0                    Free allocation.
    Non‑zero Smaller than oldSize Shrink existing allocation.
    Non‑zero Larger than oldSize  Grow existing allocation.

    internally just call realloc() since it handles a ton of this for us already
    including copying over the contents of the memory!
 * @param pointer 
 * @param oldSize 
 * @param newSize 
 * @return void* 
 */
void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        free(pointer);
        return NULL;
    }
    if (oldSize == 0) {
        return malloc(newSize);
    }

    void* newpointer = realloc(pointer, newSize);
    if (newpointer == NULL) {
        // realloc failed (out of memory probably?)
        exit(1);
    }
    return newpointer;
}
package com.craftinginterpreters.lox;

/*
 * Make a custom error class for runtime errors, taking note of the token that 
 * caused the fault. This will trigger for stuff like 3 + "cow"
 */
public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        // this super call will fill in the stack trace
        super(message);
        this.token = token;
    }
}

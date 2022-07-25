package com.craftinginterpreters.lox;

enum TokenType {
    // Single char tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COLON,

    // one or two character tokens 
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, 
    LESS, LESS_EQUAL,

    // primitives
    IDENTIFIER, STRING, NUMBER,

    // control flow
    IF, ELSE, WHILE, FOR, BREAK,

    // logical conditions
    AND, OR,

    // primitives
    TRUE, FALSE, NIL, 

    // OOP
    CLASS, THIS, SUPER,
    
    // functions
    FUN, RETURN,

    // etc.
    VAR, EOF, PRINT
}

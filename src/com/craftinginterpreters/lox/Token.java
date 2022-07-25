/**
 * This class represents a token that is read from a String
 */

package com.craftinginterpreters.lox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;

    // each token knows which line it is on (for error messages)
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Token " + type + "|" + lexeme + "|" + literal;
    }
}

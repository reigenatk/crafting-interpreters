package com.craftinginterpreters.lox;

// a static import, makes it so we don't have to type TokenType.[TOKEN_VALUE]
// everytime we wanna use a token, we can just type [TOKEN_VALUE]
// for example instead of "TokenType.AND" we just type "AND" and it knows what we mean
import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private HashMap<String, TokenType> keywordsMap = new HashMap<String, TokenType>();

    // which character did we start on
    private int start = 0;
    // which character are we currently on
    private int current = 0;

    // the line number
    private int line = 1;

    Scanner(String source) {
        this.source = source;
        initializeKeywords();
    }

    // call this method to scan all tokens from a String source
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // so start and current are used as indices into the current token being parsed
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private Boolean isAtEnd() {
        if (current >= source.length()) {
            return true;
        }
        return false;
    }

    private void scanToken() {
        // get next character
        char c = advance();

        // check for single character lexemes
        switch (c) {
            case '*': addToken(STAR); break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            // for these cases we check if its just "!" or "!=".
            case '!': 
                addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            // check for comment vs division
            case '/':
                if (match('/')) {
                    // then its a comment, so consume chars until end of line
                    // this stops right on the newline char (if there is one)
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }
                else {
                    // its just a slash token
                    addToken(SLASH);
                }
                break;
            // ignore random spaces, and for newline make sure to increase line no
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line++;
                break;
            // strings
            case '"':
                string();
                break;
            default:
                // numerical values (we support both floats and ints, but no floats starting with a . character)
                if (isDigit(c)) {
                    number();
                }
                else if (isAlphaNumeric(c)) {
                    // check for keywords, this goes until the next space (or weird character)
                    // since ' ' isnt alphanumeric
                    while (isAlphaNumeric(peek())) {
                        advance();
                    }
                    String s = source.substring(start, current);
                    if (keywordsMap.get(s) != null) {
                        // if special keyword
                        addToken(keywordsMap.get(s), s);
                    }
                    else {
                        // otherwise if not, then its a variable name or something
                        addToken(IDENTIFIER, s);
                    }

                }
                else {
                    // some weird character that's not supported by our language, like ~ for example
                    Lox.error(line, "Unexpected character " + c);
                }
                break;
        }
    }

    private void number() {
        // note, no need to do !isAtEnd here since peek returns null byte if at end, and that isn't a digit
        // so it will auto break
        while (isDigit(peek())) {
            advance();
        }
        // allow one period (for floats)
        // important restriction though- after the period we must have more numbers
        // so no numbers like "30." 
        if (peek() == '.' && isDigit(peekNext())) {
            // advance past the '.'
            advance();

            // then keep doing it again
            while (isDigit(peek())) {
                advance();
            }
        }
        // we are at end of the digit
        String num = source.substring(start, current);

        // use java's built in parseDouble to get the value
        Double num_value = Double.valueOf(num);
        addToken(NUMBER, num_value);
    }

    void initializeKeywords() {
        keywordsMap.put("and", AND);
        keywordsMap.put("class", CLASS);
        keywordsMap.put("else", ELSE);
        keywordsMap.put("false", FALSE);
        keywordsMap.put("for", FOR);
        keywordsMap.put("fun", FUN);
        keywordsMap.put("if", IF);
        keywordsMap.put("nil", NIL);
        keywordsMap.put("or", OR);
        keywordsMap.put("print", PRINT);
        keywordsMap.put("return", RETURN);
        keywordsMap.put("super", SUPER);
        keywordsMap.put("this", THIS);
        keywordsMap.put("true", TRUE);
        keywordsMap.put("var", VAR);
        keywordsMap.put("while", WHILE);
    }

    private Boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private Boolean isAlpha(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
            return true;
        }
        return false;
    }

    private Boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // look at current character
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    // look at current character
    private char peekNext() {
        if (current+1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current+1);
    }

    // for checking the next character to see if it is a two-char or one-char literal
    private Boolean match(char c) {
        if (isAtEnd()) {
            return false; 
        }
        if (source.charAt(current) == c) {
            current++;
            return true;
        }
        return false;
    }

    private char advance() {
        char ret = source.charAt(current);
        current++;
        return ret;
    }

    // for one char tokens?
    private void addToken(TokenType t) {
        addToken(t, null);
    }

    private void addToken(TokenType t, Object literal) {
        // the current token goes from the start char to the current+1 (which is already true thanks to advance)
        String text = source.substring(start, current);
        tokens.add(new Token(t, text, literal, line));
    }

    // this function gets invoked on the first " character. Parse until you reach next " character
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // this allows multi-line strings in Lox code, apparently not allowing this is harder
            if (peek() == '\n') line++;
            advance();
        }

        // take care of unterminated string case
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
        }

        // do start+1 to ignore the first " character
        String value = source.substring(start + 1, current);

        // go past the closing quote
        advance();
        addToken(STRING, value);
    }
}

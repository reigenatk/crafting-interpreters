package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;


import static com.craftinginterpreters.lox.TokenType.*;

/**
 * The parser's input is a List<Token> from the Scanner, and outputs a List<Statement> for 
 * the interpreter to evaluate during runtime. Parser's job is to create the AST
 */
public class Parser {
    private final List<Token> tokens;

    // the next token waiting to be parsed
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // new parse looks for statements, not expressions. Expressions are
    // production of statement.
    public List<Statement> parse() {
        List<Statement> program = new ArrayList<Statement>();
        
        while (!isAtEnd()) {
            program.add(declaration());
        }
        return program;
    }

    // declaration -> varDecl | statement
    private Statement declaration() {
        try {
            // if variable declaration like "var x = 3;"
            if (match(VAR)) {
                return varDeclaration();
            }

            // else if just statement (either print or exp statement)
            return statement();
        }
        catch (ParseError p) {
            // so if there was error parsing the current token, we want to 
            // synchronize to next available location to keep parsing
            // since internally, ParseError will have called Lox.error() already so
            // the main class will be notified. But we don't want to stop parsing.
            synchronize();
            return null;
        }
    }
    
    // statement -> expressionStatement | printStatement
    private Statement statement() {
        // print statement
        if (match(PRINT)) {
            
            return printStatement();
        }
        // block statement
        if (match(LEFT_BRACE)) {
            return new Statement.BlockStatement(block());
        }

        // expression statement
        return expressionStatement();
    }

    // [expression];
    private Statement expressionStatement() {
        Expression exp = expression();
        Statement stmt = new Statement.ExpressionStatement(exp);
        consume(SEMICOLON, "Expression Statement must end with a semicolon");
        return stmt;
        
    }

    // print [expression];
    private Statement printStatement() {
        Expression right_exp = expression();
        Statement stmt = new Statement.PrintStatement(right_exp);
        consume(SEMICOLON, "Print Statement must end with a semicolon");
        return stmt;
    }

    // block → "{" declaration* "}" ;
    private List<Statement> block() {
        List<Statement> codeInsideBlock = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            Statement s = declaration();
            codeInsideBlock.add(s);
        }
        
        consume(RIGHT_BRACE, "Block statement { has no closing }");
        return codeInsideBlock;
    }

    // varDecl -> "var" IDENTIFIER ("=" expression )? ";" where ? means 0 or 1
    private Statement varDeclaration() {
        Token variableName = consume(IDENTIFIER, "Variable name expected");
        // if there is an expression that this variable should be set to
        Expression exp = null;
        if (match(EQUAL)) {
            exp = expression();
        }
        consume(SEMICOLON, "Variable Statement must end with a semicolon");
        return new Statement.VariableDeclaration(variableName, exp);
    }

    // Expression -> assignment
    private Expression expression() {
        return assignment();
    }

    // assignment → IDENTIFIER "=" assignment | equality;
    // this one's tricky, check out page 122 for explanation
    // essentially though we parse stuff before the equals sign as an expression
    // check if its a variable, if it is then we grab its token
    // and we evaluate rhs, then create the new Expression.Assignment()
    // and if no equals sign then OK its just equality()
    // the reason we have to do it this way is because LHS could be complex
    // like x.y.z or something, and that is also a valid expression statement so we can abuse that
    private Expression assignment() {
        Expression lhs = equality();
        if (match(EQUAL)) {
            Token equalsToken = previous();
            Expression rhs = assignment();
            if (lhs instanceof Expression.Variable) {
                // valid assignment statement
                Expression.Variable variableName = (Expression.Variable) lhs;
                Token variable = variableName.name;
                return new Expression.Assignment(variable, rhs);
            }
            else {
                // invalid assignment statement
                error(equalsToken, "Invalid assignment");
            }
        }
        return lhs;
    }

    // Equality -> Comparison ( ("!=" | "==") Comparison )*
    // statements like "x == y" or "x == y != z"
    private Expression equality() {
        Expression first_comparison = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression second_comparison = comparison();

            // combines the first kind of "equality" comparison it sees and packs it 
            // into the first expression
            first_comparison = new Expression.Binary(first_comparison, operator, second_comparison);
        }
        return first_comparison;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expression comparison() {
        Expression first_term = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expression second_term = term();
            first_term = new Expression.Binary(first_term, operator, second_term);
        }
        return first_term;
    }

    // term → factor ( ( "-" | "+" ) factor )* ;
    private Expression term() {
        Expression first_factor = factor();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expression second_factor = factor();
            first_factor = new Expression.Binary(first_factor, operator, second_factor);
        }
        return first_factor;
    }

    // factor → unary ( ( "/" | "*" ) unary )* ;
    private Expression factor() {
        Expression first_unary = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression second_unary = unary();
            first_unary = new Expression.Binary(first_unary, operator, second_unary);
        }
        return first_unary;
    }

    // unary → ( "!" | "-" ) unary | primary ;
    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token t = previous();
            Expression first_unary = unary();
            return new Expression.Unary(t, first_unary);
        }
        else {
            return primary();
        }
    }
    // primary → NUMBER | STRING | "true" |
    private Expression primary() {
        // these keywords don't have value assigned to them from Scanner
        // so we attach them manually here
        if (match(NIL)) return new Expression.Literal(null);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(FALSE)) return new Expression.Literal(false);

        // we need to do previous here because recall, match() does advance the counter
        // forwards in the List of Tokens
        if (match(NUMBER, STRING)) return new Expression.Literal(previous().literal);

        // if its none of these patterns before, then we say it must be a variable token
        if (match(IDENTIFIER)) return new Expression.Variable(previous());

        // this part is interesting- basically, if we have a ( then 
        // it will be picked up as a literal. It will then recurse on the right side of 
        // the ( and eventually primary will be called again on a ')' character. Knowing that 
        // occurs we can then check for that in the left brace's code. Weird stuff.
        // this way we can check that every left brace has a corresponding closing right brace

        if (match(LEFT_PAREN)) {
            Expression expr = expression();
            consume(RIGHT_PAREN, "opening left brace must be closed");

            // and we return a grouping expression for this.
            return new Expression.Grouping(expr);
        }

        // if we get here, it means we have encountered a token that cannot match any 
        // expression. So we just throw an error
        throw error(peek(), "Expected expression.");
    }


    // ========================================== HELPER METHODS ========================== // 
    /**
     * checks if the next token matches any of the supplied TokenTypes. If so, return true
     * else return false. Also consumes the token if it matches
     * @param t
     * @return
     */
    private boolean match(TokenType... t) {
        for (TokenType tt : t) {
            if (check(tt)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // checks if the next char is of type t, if it is, increment cnt and return it
    // if it isnt throw the error message
    private Token consume(TokenType t, String err_msg) {
        if (check(t)) {
            return advance();
        }

        throw error(previous(), err_msg);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return tokens.get(current).type == EOF;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    // ===================================== END OF HELPER METHODS ========================== //

    private static class ParseError extends RuntimeException {}

    // called by the parser. We don't want to terminate the program though.
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // synchronization method for when we hit a syntax error
    // we call this after catching a ParseError
    private void synchronize() {
        // skip past the erroring token
        advance();
        while (!isAtEnd()) {
            // if right after semicolon token, then its OK again probably
            if (previous().type == SEMICOLON) return;

            // or if the current token is a starting keyword, then we're probably at start of 
            // a new expression and we can claim we are syncronized again
            switch (peek().type) {
                case FOR:
                case CLASS:
                case IF: 
                case FUN:
                case RETURN:
                case WHILE:
                case PRINT:
                case VAR:
                    return;
            }
            advance();
        }
    }
}

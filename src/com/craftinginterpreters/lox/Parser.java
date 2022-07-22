package com.craftinginterpreters.lox;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


import static com.craftinginterpreters.lox.TokenType.*;

/**
 * The parser's input is a List<Token> from the Scanner, and outputs a List<Statement> for 
 * the interpreter to evaluate during runtime. Parser's job is to create the AST
 */
public class Parser {
    private final List<Token> tokens;

    // the next token waiting to be parsed
    private int current = 0;

    // the number of loops deep that we are in. We need this for break statements
    private int loopDepth = 0;

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

        // if statement
        if (match(IF)) {
            return ifStatement();
        }

        // while statement
        if (match(WHILE)) {
            return whileStatement();
        }

        if (match(BREAK)) {
            if (loopDepth == 0) {
                // bad! We must be in a loop of some sort to break
                error(previous(), "Break statement must be in a for or while loop to work");
            }
            return breakStatement();
        }

        // for statement
        if (match(FOR)) {
            return forStatement();
        }

        // function DECLARATION
        if (match(FUN)) {
            return functionDeclaration();
        }

        // return statements
        if (match(RETURN)) {
            return returnStatement();
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

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
    private Statement ifStatement() {
        consume(LEFT_PAREN, "if statement missing '(' token");
        Expression exp = expression();
        consume(RIGHT_PAREN, "if statement missing ')' token");
        Statement ifContents = statement();

        // if there's an else branch
        Statement elseContents = null;
        while(check(ELSE) && !isAtEnd()) {
            advance(); // consume else token
            elseContents = statement();
        }

        return new Statement.IfStatement(exp, ifContents, elseContents);
    }

    // whileStmt → "while" "(" expression ")" statement 
    private Statement whileStatement() {
        try {
            loopDepth++;
            consume(LEFT_PAREN, "while statement missing '(' token");
            Expression exp = expression();
            consume(RIGHT_PAREN, "while statement missing ')' token");
            Statement whileContents = statement();
            return new Statement.WhileStatement(exp, whileContents);
        }
        finally {
            loopDepth--;
        }

    }
    // forStmt → "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
    // just an example, for (int i = 1; i < 3; i++)
    // initial: "int i = 1"
    // condition: "i < 3"
    // increment: "i++"
    private Statement forStatement() {
        try {
            loopDepth++;
            Statement initial = null;
            consume(LEFT_PAREN, "for statement missing '(' token");
            if (match(VAR)) {
                initial = varDeclaration();
            }
            else if (match(SEMICOLON)) {
                // then no first part
            }
            else {
                initial = expressionStatement();
            }
            Expression condition = null;
            if (match(SEMICOLON)) {
                // no second part
            }
            else {
                condition = expression();
                consume(SEMICOLON, "for statement missing ';' token");
            }
            Expression increment = null;
            if (match(RIGHT_PAREN)) {
                // no third part
            }
            else {
                increment = expression();
                consume(RIGHT_PAREN, "for statement missing ')' token");
            }
            Statement code = statement();
    
            // now we have to desugar the condition into a single Expression
            // the increment is like a little code that runs after each iteration of a while loop
            // we can represent that by making a Block statement and adding to the end the increment
            // also we don't support ++ or anything so we should write it like "i = i+1" for now
            if (increment != null) {
                code = new Statement.BlockStatement(Arrays.asList(code, 
                new Statement.ExpressionStatement(increment)));
            }
    
            if (condition == null) condition = new Expression.Literal(true);
            Statement body = new Statement.WhileStatement(condition, code);
    
            // the initial condition should run before the loop takes place
            // furthermore it should be outside the whilestatement, since it only runs once
            if (initial != null) {
                body = new Statement.BlockStatement(Arrays.asList( 
                initial, body));
            }
    
            // so to summarize, a For loop is just a While loop on a block statement with
            // an initial condition before it, and a nested increment block inside
            return body;
        }
        finally {
            loopDepth--;
        }
        
    }

    // funDecl → "fun" function ;
    private Statement functionDeclaration() {
        return function("function");
    }

    // function → IDENTIFIER "(" parameters? ")" block
    private Statement function(String kind_of_function) {
        Token funcName = consume(IDENTIFIER, "New " + kind_of_function + " must have a name");
        consume(LEFT_PAREN, "New function defienition must have (");

        List<Token> params = new ArrayList<>();

        // consume parameters, so in something like fun stuff(a,b,c) {} its the "a,b,c" part
        // first check to see if we even have arguments
        if (!check(RIGHT_PAREN)) {
            Token param = advance();
            params.add(param);
            while (match(COMMA)) {
                param = advance();
                params.add(param);

                // check that again not more than 255 args, if so, make sure to print an error
                if (params.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments in function definition");
                }
            }
        }
        else {
            // no arguments
        }
        consume(RIGHT_PAREN, "Missing matching ')' for function definition parameters");

        // little tricky, dont forget to consume { since block() expects it to be consumed
        consume(LEFT_BRACE, "Expecting function body starting with {");
        List<Statement> functionCode = block();
        return new Statement.FunctionStatement(funcName, params, functionCode);
    }

    // returnStmt → "return" expression? ";" 
    private Statement returnStatement() {
        Expression e = null;
        if (check(SEMICOLON)) {
            // then its just "return;"
        }
        else {
            e = expression();
        }
        consume(SEMICOLON, "Missing semicolon after return statement");
        return new Statement.ReturnStatement(e);
    }

    private Statement breakStatement() {
        consume(SEMICOLON, "Break Statement must end with a semicolon");
        return new Statement.BreakStatement();
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

    // assignment → IDENTIFIER "=" assignment | logic_or
    // this one's tricky, check out page 122 for explanation
    // essentially though we parse stuff before the equals sign as an expression
    // check if its a variable, if it is then we grab its token
    // and we evaluate rhs, then create the new Expression.Assignment()
    // and if no equals sign then OK its just equality()
    // the reason we have to do it this way is because LHS could be complex
    // like x.y.z or something, and that is also a valid expression statement so we can abuse that
    private Expression assignment() {
        Expression lhs = logic_or();
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

    // logic_or → logic_and ( "or" logic_and )* ;
    private Expression logic_or() {
        Expression lhs = logic_and();
        while (match(OR)) {
            Token or = previous();
            Expression e = logic_and();
            lhs = new Expression.Logical(lhs, or, e);
        }
        return lhs;
    }

    // logic_and → equality ( "and" equality )* ;
    private Expression logic_and() {
        Expression lhs = equality();
        while (match(AND)) {
            Token and = previous();
            Expression e = equality();
            lhs = new Expression.Logical(lhs, and, e);
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

    // unary → ( "!" | "-" ) unary | call ;
    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token t = previous();
            Expression first_unary = unary();
            return new Expression.Unary(t, first_unary);
        }
        else {
            return call();
        }
    }

    // call → primary ( "(" arguments? ")" )* 
    private Expression call() {
        Expression e = primary();
        while (match(LEFT_PAREN)) {
            // recursively create the expression (nested), kinda like how we did for loops
            e = arguments(e);
        }
        return e;
    }

    // arguments → expression ( "," expression )* 
    private Expression arguments(Expression e) {
        List<Expression> args = new ArrayList<Expression>();

        // if there are even arguments
        if (peek().type != RIGHT_PAREN) {
            Expression arg1 = expression();
            args.add(arg1);
            while (match(COMMA)) {
                if (args.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments");
                }
                Expression arg2 = expression();
                args.add(arg2);
            }
        }

        // match closing paren for this call
        Token rightParen = consume(RIGHT_PAREN, "function call arguments not terminated with )");
        return new Expression.Call(e, args, rightParen);
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

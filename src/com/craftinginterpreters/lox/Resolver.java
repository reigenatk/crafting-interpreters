package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.craftinginterpreters.lox.Expression.*;
import com.craftinginterpreters.lox.Statement.*;

/**
 * This is a class for performing static analysis on code, mainly to resolve variables
 */
public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;

    // this is kinda like the environments linkedlist of hashmaps? except in a stack structure
    // its map of string to bool because bool is whether or not this symbol has been defined
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // keep track of whether or not we are in a function
    private FunctionType currentFunction = FunctionType.NONE;

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD
    }

    Resolver(Interpreter i) {
        interpreter = i;
    }

    private void resolve(Statement s) {
        s.accept(this);
    }

    private void resolve(Expression e) {
        e.accept(this);
    }

    public void resolve(List<Statement> statements) {
        for (Statement s : statements) {
            resolve(s);
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        // if no scopes to declare in then do nothing
        // recall that if we are in global scope, scopes will be empty. 
        // since scopes only gets populated from beginScope(), which is only called by functions 
        // and block statements!
        if (scopes.isEmpty()) return;

        // otherwise get the top scope
        Map<String, Boolean> scope = scopes.peek();
        
        // check that it has not yet been declared
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already exists variable with name " + name.lexeme + " in this scope.");
        }

        // and put the name of the token in. 
        // the False means we have not finished resolving this variables' initializer
        System.out.println("Declaring " + name.lexeme);
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;

        System.out.println("Defining " + name.lexeme);
        // now mark as true to say that it is defined
        scopes.peek().put(name.lexeme, true);
    }

    // this goes up through all the scopes and tries to resolve this
    // the Token name is just the name of the variable, aka expression.name
    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size()-1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                // second arg is number of scopes between current scope and the one where it was found
                // send this resolve info to interpreter so it can use the info during runtime
                System.out.println("Calling resolve with expression: " + name.lexeme + " and distance " + (scopes.size() - i - 1));
                interpreter.resolve(expression, scopes.size() - i - 1);
                return;
            }
        }
    }

    private void resolveFunction(FunctionStatement f, FunctionType type) {
        // save old function type
        FunctionType oldFunction = currentFunction;
        // set the current function we are in
        currentFunction = type;

        // add a new scope for this function
        beginScope();

        // resolve params- not sure why we define it too, doesnt func declaration just declare?
        for (Token t : f.args) {
            declare(t);
            define(t);
        }

        // and then resolve all function code
        for (Statement s : f.code) {
            resolve(s);
        }
        endScope();
        // restore old function type
        currentFunction = oldFunction;
    }

// =============================== All visits that modify scope or symbols ======================= //
    @Override
    public Void visitBlockStatementStatement(BlockStatement statement) {
        beginScope();
        resolve(statement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(VariableDeclaration statement) {
        declare(statement.name);

        // resolve RHS
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    // aka just accessing the variable itself
    @Override
    public Void visitVariableExpression(Variable expression) {
        // if there are no scopes or the variable is declared but not defined, we report it
        // this avoids any var a = a business
        
        // System.out.println("haha " + expression.name.lexeme + " and " + scopes.peek().get(expression.name.lexeme));
        if (!scopes.empty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) {
            Lox.error(expression.name, "Variable " + 
            expression.name.lexeme + " is not defined yet");
        }

        // otherwise if we can find it
        resolveLocal(expression, expression.name);
        return null;
    }
    
    // a = 3;
    @Override
    public Void visitAssignmentExpression(Assignment expression) {
        // first resolve RHS
        resolve(expression.value);

        // then resolve the local variable
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitFunctionStatementStatement(FunctionStatement statement) {
        declare(statement.funcName);
        define(statement.funcName);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitClassDeclarationStatement(ClassDeclaration statement) {
        // we must declare/define the class in this scope
        declare(statement.nameOfClass);
        define(statement.nameOfClass);

        // resolve all the methods
        for (FunctionStatement f : statement.methods) {
            resolveFunction(f, FunctionType.METHOD);
        }
        return null;
    }

    // ===================== All other visits, most of these just call resolve() ===================== //
    @Override
    public Void visitExpressionStatementStatement(ExpressionStatement statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitIfStatementStatement(IfStatement statement) {
        resolve(statement.condition);
        resolve(statement.ifCode);
        if (statement.elseCode != null) {
            resolve(statement.elseCode);
        }
        return null;
    }

    @Override
    public Void visitPrintStatementStatement(PrintStatement statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitReturnStatementStatement(ReturnStatement statement) {
        if (currentFunction == FunctionType.NONE) {
            // bad
            Lox.error(statement.returnKeyword, "Return keyword must take place in a function");
        }
        resolve(statement.exp);
        return null;
    }

    @Override
    public Void visitWhileStatementStatement(WhileStatement statement) {
        resolve(statement.condition);
        resolve(statement.code);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Call expression) {
        resolve(expression.callee);
        for (Expression e : expression.args) {
            resolve(e);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpression(Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Literal expression) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(Unary expression) {
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitLogicalExpression(Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitBreakStatementStatement(BreakStatement statement) {
        return null;
    }

    @Override
    public Void visitGetExpression(Get expression) {
        resolve(expression.object);
        return null;
    }

    /*
     * Again, like Expr.Get, the property itself is dynamically evaluated, so there’s
       nothing to resolve there. All we need to do is recurse into the two
       subexpressions of Expr.Set, the object whose property is being set, and the value
       it’s being set to
     */
    @Override
    public Void visitSetExpression(Set expression) {
        resolve(expression.value);
        resolve(expression.object);
        return null;
    }

}

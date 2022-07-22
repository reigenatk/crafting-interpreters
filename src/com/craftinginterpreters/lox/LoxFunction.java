package com.craftinginterpreters.lox;

import java.util.List;

/**
 * The class to hold our defined functions, implements LoxCallable so we can use call()
 */
public class LoxFunction implements LoxCallable {

    private final Statement.FunctionStatement function;

    LoxFunction(Statement.FunctionStatement f) {
        function = f;
    }

    // how many arguments does this function take in
    @Override
    public int arity() {
        return function.args.size();
    }

    // represents a function execution
    @Override
    public Object call(Interpreter i, List<Object> args) {
        // create a new environment for this function execution
        Environment funcEnv = new Environment(i.currentEnv);

        // define all the parameters in this new environment
        for (int j = 0; j < args.size(); j++) {
            funcEnv.addNewVariable(function.args.get(j).lexeme, args.get(j));
        }

        // call interpreter function to execute block of code with function environment
        i.executeBlock(function.code, funcEnv);
        return null;
    }

    @Override
    public String toString() {
        return "<function " + function.funcName.lexeme + ">";
    }
}

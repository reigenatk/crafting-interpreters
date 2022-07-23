package com.craftinginterpreters.lox;

import java.util.List;

import com.craftinginterpreters.lox.Interpreter.ReturnException;

/**
 * The class to hold our defined functions, implements LoxCallable so we can use call()
 */
public class LoxFunction implements LoxCallable {

    // this has the args, name of function, and code inside the function
    private final Statement.FunctionStatement function;

    // this will hold the last environment that this function had, or initialize it if not yet made
    private final Environment closure;

    LoxFunction(Statement.FunctionStatement f, Environment closure) {
        this.closure = closure;
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

        // define a new environment for func execution that has closure as parent env
        Environment funcEnv = new Environment(closure);

        // define all the parameters in this new environment
        for (int j = 0; j < args.size(); j++) {
            funcEnv.addNewVariable(function.args.get(j).lexeme, args.get(j));
        }

        // call interpreter function to execute block of code with function environment
        // if we hit a return statement before code execution finishes, this try/catch will
        // return the specified value in the return statement
        try {
            i.executeBlock(function.code, funcEnv);
        }
        catch (ReturnException r) {
            return r.returnValue;
        }
        
        // by default functions return null (or nil in Lox)
        return null;
    }

    @Override
    public String toString() {
        return "<function " + function.funcName.lexeme + ">";
    }
}

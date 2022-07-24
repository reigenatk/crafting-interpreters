package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

import com.craftinginterpreters.lox.Statement.FunctionStatement;

// represents a class in Lox
public class LoxClass implements LoxCallable {
    private Map<String, LoxFunction> methods;
    private String nameOfClass;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        nameOfClass = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        
        return "class " + nameOfClass;
    }

    @Override
    public int arity() {
        // classes have no args
        return 0;
    }

    @Override
    public Object call(Interpreter i, List<Object> args) {
        // calling a class = creating an instance of the object with these args (at least in Lox)
        // this will get called by visitCallExpression in Interpreter.java!
        LoxInstance instance = new LoxInstance(this);
        return instance;
    }

    public LoxFunction findMethod(Token methodName) {
        return methods.get(methodName.lexeme);
    }

}

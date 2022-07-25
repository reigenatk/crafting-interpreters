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
        // the arity of a class is how many arguments its constructor has
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            return initializer.arity();
        }
        // if no constructor found then 0
        return 0;
    }

    @Override
    public Object call(Interpreter i, List<Object> args) {
        // calling a class = creating an instance of the object with these args (at least in Lox)
        // this will get called by visitCallExpression in Interpreter.java!
        LoxInstance instance = new LoxInstance(this);

        // check whether there's a constructor on the class, if so, 
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            // then bind and run it. I think we need to bind because if you look at 
            // LoxInstance.getField, the only other instance where bind is being called,
            // its done during the returning of a method. Which will be called. So here 
            // its like we're gonna run, therefore to stay consistent with getField we need to bind too.
            initializer.bind(instance).call(i, args);
        }
        return instance;
    }

    // called by LoxInstance.getField()
    public LoxFunction findMethod(String methodName) {
        return methods.get(methodName);
    }

}

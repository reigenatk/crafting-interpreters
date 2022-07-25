package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

import com.craftinginterpreters.lox.Statement.FunctionStatement;

// represents a class in Lox
public class LoxClass implements LoxCallable {
    private Map<String, LoxFunction> methods;
    private String nameOfClass;
    private LoxClass superclass;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        nameOfClass = name;
        this.methods = methods;
        this.superclass = superclass;
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

    // called by LoxInstance.getField(). Searches the methods hashmap for a method of this name
    public LoxFunction findMethod(String methodName) {

        LoxFunction lf = methods.get(methodName);
        if (lf == null) {
            // perhaps the superclass knows?
            if (superclass != null) {
                return superclass.findMethod(methodName);
            }
            else {
                // uh oh, no one knows
                Token bsToken = new Token(TokenType.COLON, ":", ":", -1);
                return null;
                // throw new RuntimeError(bsToken, "Couldn't find method of name " + methodName);
            }
        }
        return lf;
    }

}

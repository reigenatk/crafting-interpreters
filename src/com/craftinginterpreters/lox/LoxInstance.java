package com.craftinginterpreters.lox;

import java.util.HashMap;

// represents the instance of a class in Lox
public class LoxInstance {
    private LoxClass classType;

    // the fields on this class
    private final HashMap<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass lc) {
        classType = lc;
    }

    @Override
    public String toString() {
        return "instance of " + classType.toString();
    }

    // ok so this is tricky but essentially since in Resolver we said that the this keyword is defined
    // in the class scope upon class declaration, and the locals hashmap in Interpreter will expect this,
    // then we need to also create this extra envrionment inside Interpreter (which calls LoxInstance.getField
    // in visitGetExpression). The resolver and Interpreter must match up. Otherwise we 
    // will get errors. So that's why we do lf.bind().
    public Object getField(Token nameOfMember) {
        // check that field actually exists on the class
        if (fields.containsKey(nameOfMember.lexeme)) {
            return fields.get(nameOfMember.lexeme);
        }

        // Ok, its not a field. Check if its a method stored on the class
        LoxFunction lf = classType.findMethod(nameOfMember.lexeme);
        // use .bind() to add the "this" symbol using an extra layer of environment
        if (lf != null) return lf.bind(this);

        throw new RuntimeError(nameOfMember, "No property " + nameOfMember.lexeme + " on " + classType);
    }

    public void setField(Token nameOfMember, Object newValue) {
        // ok no check here since field doesnt have to yet exist on the class
        fields.put(nameOfMember.lexeme, newValue);
    }
}

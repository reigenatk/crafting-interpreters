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

    public Object getField(Token nameOfMember) {
        // check that field actually exists on the class
        if (fields.containsKey(nameOfMember.lexeme)) {
            return fields.get(nameOfMember.lexeme);
        }

        // Ok, its not a field. Check if its a method stored on the class
        LoxFunction lf = classType.findMethod(nameOfMember);
        if (lf != null) return lf;
        
        throw new RuntimeError(nameOfMember, "No property " + nameOfMember.lexeme + " on " + classType);
    }

    public void setField(Token nameOfMember, Object newValue) {
        // ok no check here since field doesnt have to yet exist on the class
        fields.put(nameOfMember.lexeme, newValue);
    }
}

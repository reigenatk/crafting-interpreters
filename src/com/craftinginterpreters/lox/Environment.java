package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

// the environment holds all declared variables during the execution of a Lox program
// internally its just a hashmap
public class Environment {
    // reference to parent environment
    final Environment parentEnv;
    
    private final Map<String, Object> variableToValue = new HashMap<>();

    // global scope has no parent environment
    Environment() {
        parentEnv = null;
    }

    // for any other scopes, either global scope will be its parent environment
    // or some other block scope
    Environment(Environment parentEnv) {
        this.parentEnv = parentEnv;
    }

    public void addNewVariable(String varName, Object val) {
        variableToValue.put(varName, val);
    }

    public Object getVariableValue(Token varName) {
        if (!variableToValue.containsKey(varName.lexeme)) {
            if (parentEnv != null) {
                // not global scope yet, then try asking parent env if it knows
                return parentEnv.getVariableValue(varName);
            }
            else {
                // global scope still doesn't know
                throw new RuntimeError(varName, "Can't access variable: " + varName.lexeme + " is not defined");
            }
        }
        return variableToValue.get(varName.lexeme);
    }

    public void changeExistingVariable(Token varName, Object val) {
        if (!variableToValue.containsKey(varName.lexeme)) {
            if (parentEnv != null) {
                // not global scope yet, then try asking parent env if it knows
                parentEnv.changeExistingVariable(varName, val);
            }
            else {
                // global scope still doesn't know
                throw new RuntimeError(varName, "Can't change existing variable: " + varName.lexeme + " is not defined");
            }
        }
        variableToValue.put(varName.lexeme, val);
    }
}

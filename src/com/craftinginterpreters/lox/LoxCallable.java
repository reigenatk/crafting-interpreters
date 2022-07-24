package com.craftinginterpreters.lox;

import java.util.List;

// a callable interface
interface LoxCallable {

    int arity();

    Object call(Interpreter i, List<Object> args);

    String toString();
}

## Crafting Interpreters

I decided to learn more about interpreters, compilers, etc. as it seemed like a fun challenge. I'm going to work through [this very well written book](https://github.com/munificent/craftinginterpreters) and hopefully have something cool to show by the end of it.

The language we will make is called **Lox**, and it is very similar to C in syntax. 

## Rules

Assume the same as in C, except:

- Functions are declared using the `fun` keyword
- There are no types, this is dynamically typed language
- For OOP, create instances of a class by calling the name of the class like a function
- Class definitions can only have functions inside of them, which will call methods. They must not have the `fun` keyword in front of them. 
- The constructor must be named `init()`
- Supports `or`, `and`, `this` and `super` statements
- Check out the examples if still confused. You can run them by doing 

We will implement two interpreters, the first one in Java, which we will call JLox.

## JLox

Some notes on the important classes:

1. The scanner (formally a lexer) works by scanning an input string token by token and creating Token objects for each one. This scans for all the major keywords defined in TokenType.java

2. It then passes a list of **Tokens** to the Parser, which will create an Abstract Syntax Tree (AST) as well as detect any syntax errors. Internally, the syntax tree is represented as a List of Statements

3. From there, the Parser sends the List<Statements> to the Resolver, which performs static analysis of the code. Most importantly it resolves all the local variables to their appropriate scopes, as well as detects any simple syntax errors (for example putting a return statement outside of a function)

3. Finally the same List<Statements> is sent to the Intepreter, also known as the **runtime**. This actually executes the program using the JVM. For instance, an Expression. Literal would be resolved to a Java Integer. This is where LoxClass, LoxFunction, and LoxInstance objects are created to represent classes, functions and instances of objects. Also, we create scopes as we see fit, to successfully encapsulate variables.

## CLox

We will implement a C version to increase performance. The goal is to create bytecode instead of directly parsing the syntax tree. 

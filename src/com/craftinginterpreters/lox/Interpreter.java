package com.craftinginterpreters.lox;
import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

// this marks the beginning of the RUNTIME
// the class that actually takes the expressions from the syntax tree
// and EVALUATES them in Java. Result is always an
// Object (which can be any of the primitive data types)
// this allows for dynamic typing, meaning we only resolve types here.
public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    // the global environment, with our native functions and variables in it
    final public Environment globals = new Environment();

    // the current innermost scope's environment (initially set to globals)
    public Environment currentEnv = globals;

    // for breaks that aren't placed properly
    private static class BreakException extends RuntimeException {}

    Interpreter() {
        // instantiate native functions in global environment
        globals.addNewVariable("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter i, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0; // seconds since Unix Epoch
            }

            @Override
            public String toString() {
                return "<native function \"clock\">";
            }
        });
    }

    // evaluate calls accept, which then depending on which expression e is,
    // calls one of the visit methods below.
    private Object evaluate(Expression s) {
        return s.accept(this);
    }

    // this is like evaluate but for statements! An expression is like 3+4, a statement is like
    // 3+4; for example. We need this because we otherwise wouldn't have an way to call
    // accept (aka actually do the visitor pattern) with for statements
    private void execute(Statement s) {
        s.accept(this);
    }

    public void executeBlock(List<Statement> codeBlock, Environment env) {
        Environment oldEnv = currentEnv;
        try {
            currentEnv = env;
            for (Statement codeLine : codeBlock) {
                execute(codeLine);
            }
        }
        finally {
            currentEnv = oldEnv;
        }
    }

    // this takes in a list of statements, otherwise known as a program
    // and calls accept on each one, which will start running the statement visit methods below
    public void interpret(List<Statement> program) {
        try {
            for (Statement s : program) {
                execute(s);
            }
        }
        catch (RuntimeError e) {
            Lox.error(e);
        }
    }

    // converting Java back to Lox (null in Java is nil in Lox)
    // also making sure there's no .0 at the end for integers
    // since in Lox we're representing all numbers as doubles, integers will 
    // appears as 3.0 for example, and we don't want to output that. We want "3"
    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
    // ================================= Start Statement Visits ========================= //
    
    // this is something like "3+4;" and in that case we just evaluate it and move on. 
    // Nothing special actually happens
    @Override
    public Void visitExpressionStatementStatement(Statement.ExpressionStatement statement) {
        stringify(evaluate(statement.expression));

        // return null to satisfy Void
        return null;
    }

    // this should Print in java
    @Override
	public Void visitPrintStatementStatement(Statement.PrintStatement statement) {
        Object val = evaluate(statement.expression);

        // we should stringify first just like we did in interpret()
        System.out.println(stringify(val));
        return null;
    }

    // this should set the variable using Environment.addNewVariable
    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement) {
        // value of variable
        Object value = null;

        // initializer could be null, something like "var x;" for example
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }
        
        currentEnv.addNewVariable(statement.name.lexeme, value);
        return null;
    }

    // this represents a new block { }, wherein we must create a new Environment to store
    // the variables in this scope.
    @Override
    public Void visitBlockStatementStatement(Statement.BlockStatement statement) {
        Environment blockEnv = new Environment(this.currentEnv);
        executeBlock(statement.statements, blockEnv);
        return null;
    }

    // this represents an if statement. We evaluate the condition and then execute certain code
    // depending on the result of that condition evaluation
    @Override
    public Void visitIfStatementStatement(Statement.IfStatement statement) {
        // evaluate the condition
        Object condition = evaluate(statement.condition);
        if (isTruthy(condition)) {
            // then execute all the statements in the if block
            execute(statement.ifCode);
        }
        else {
            // so if false, check to see if there's an else
            if (statement.elseCode != null) {
                execute(statement.elseCode);
            }
        }
        // reaches here if if-statement was false and there's no else statement
        return null;
    }

    // when we visit a while statement, we should evaluate the condition as long as its true
    @Override
    public Void visitWhileStatementStatement(Statement.WhileStatement statement) {
        while (isTruthy(evaluate(statement.condition))) {
            // then execute all the statements in the if block
            // also if any break statements occur then they will throw a BreakException
            // we can then put a try catch here to immediately terminate the loop if such an exception occurs
            try {
                execute(statement.code);
            }
            catch (BreakException b) {
                return null;
            }
        }
        return null;
    }

    // when visiting a break, we throw an exception immediately to exit the loop that we're in
    public Void visitBreakStatementStatement(Statement.BreakStatement statement) {
        throw new BreakException();
    }

    // declaring a function with its definition. Just put the LoxFunction object into the 
    // current environment, and when function call happens it will use visitVariableExpression()
    // internally to resolve the name of the function back to the LoxFunction
    // in other words, functions are just like variables in that we store them in environment upon 
    // declaration!
    public Void visitFunctionStatementStatement(Statement.FunctionStatement statement) {
        LoxFunction lf = new LoxFunction(statement);
        currentEnv.addNewVariable(statement.funcName.lexeme, lf);
        return null;
    }

    // ================================= End Statement Visits ========================= //


    // ================================= Start Expression Visits ========================= //

    // lots of stuff here: comparison, equality, and less/greater than (or equal to)
    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left_exp_val = evaluate(expression.left);
        Object right_exp_val = evaluate(expression.right);
        switch (expression.operator.type) {
            case MINUS:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val - (double) right_exp_val;
            case PLUS:
                // support string concatenation in Lox
                if (left_exp_val instanceof String || right_exp_val instanceof String) {
                    if (left_exp_val instanceof Double) {
                        left_exp_val = stringify(left_exp_val);
                    }
                    if (right_exp_val instanceof Double) {
                        right_exp_val = stringify(right_exp_val);
                    }
                    return (String) left_exp_val + (String) right_exp_val;
                }
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val + (double) right_exp_val;
            case STAR:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val * (double) right_exp_val;
            case SLASH:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                // no divison by zero
                if ((double) right_exp_val == 0) {
                    throw new RuntimeError(expression.operator,
                    "Division by zero is not permitted");
                }
                return (double) left_exp_val / (double) right_exp_val; 
            case LESS:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val < (double) right_exp_val;
            case LESS_EQUAL:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val <= (double) right_exp_val;
            case GREATER:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val > (double) right_exp_val;
            case GREATER_EQUAL:
                verifyNumericalValues(expression.operator, left_exp_val, right_exp_val);
                return (double) left_exp_val >= (double) right_exp_val;
            case EQUAL_EQUAL:
                return isEqual(left_exp_val, right_exp_val);
            case BANG_EQUAL:
                return !isEqual(left_exp_val, right_exp_val);
                
        }
        return null;
    }

    // a grouping evaluates to itself. (3+2-1) in Lox = evaluate(3+2-1)
    // so we must recursively call evaluate
    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    // a literal evaluates to itself. For example, 3 in Lox is just 3 in java
    // or "hello" is just "hello" in java
    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    // we only have - and ! in Lox
    // so for example -31 or !24
    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right_value = evaluate(expression.right);
        if (expression.operator.type == MINUS) {
            // do a cast here because otherwise - is undefined on Object
            verifyNumericalValues(expression.operator, right_value);
            return -(double)right_value;
        }
        else if (expression.operator.type == BANG) {
            // well !(anything nonzero) = 0, and !0 = 1
            // we need to define what is nonzero in Lox though, aka what is "truthy"
            // for now we say anything other than nil (which is null) and false is nonzero
            return !isTruthy(right_value);
        }
        // dead code for compiler
        return null;
    }

    // assuming "var x = 3" was ran before this, then doing "x;" a line later should return 3
    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return currentEnv.getVariableValue(expression.name);
    }

    // something like "x=3;" returns 3 believe it or not
    @Override
    public Object visitAssignmentExpression(Expression.Assignment expression) {
        Object rhs = evaluate(expression.value);
        currentEnv.changeExistingVariable(expression.name, rhs);
        return rhs;
    }

    // OK so if we are doing an AND, then the moment we see a false we know answer is false
    // similarly for OR, the moment we see true, the answer is true
    // otherwise, result depends on the evaluation of the right expression!
    // this code is left associative which is also good
    // also note that we aren't returning true or false, but rather the expression itself
    // this let's us support stuff like "print "hi" or 2" => "hi"
    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = evaluate(expression.left);
        if (expression.operator.type == AND) {
            if (!isTruthy(left)) {
                return left; // false
            }
        }
        else if (expression.operator.type == OR) {
            if (isTruthy(left)) {
                return left; // true
            }
        }

        // ooo this is interesting
        return evaluate(expression.right);
    }

    // calling a function!
    public Object visitCallExpression(Expression.Call expression) {
        List<Expression> args = expression.args;

        // evaluate each paramter!
        List<Object> argsEvaluated = new ArrayList<>();
        for (Expression e : args) {
            Object o = evaluate(e);
            argsEvaluated.add(o);
        }

        // evaluate the callee, also known as the name of the function
        Object callee = evaluate(expression.callee);

        // cast it to a LoxCallable (but first check that it is an actual function we defined already in Lox)
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expression.closingParenthesis, "Callee is not valid function name");
        }

        LoxCallable function = (LoxCallable) callee;

        // make sure the number of arguments are the same, if not throw runtime error
        if (args.size() != function.arity()) {
            throw new RuntimeError(expression.closingParenthesis, "Number of arguments must be " + 
            function.arity() + " but got " + args.size() + " arguments instead");
        }

        return function.call(this, argsEvaluated);
    }

    // ================================= End Expression Visits ========================= //

    // this prevents stuff like -"horse" or 3 + "cow"
    // also note this is a void function because it throws an exception
    private void verifyNumericalValues(Token operator, Object... values) {
        for (Object o : values) {
            if (!(o instanceof Double)) {
                throw new RuntimeError(operator,
                 "Operation must take numerical arguments");
            }
        }
    }

    // this determines the notion of equality in Lox
    // we will use java's .equals() method on most primitive classes to 
    // determine this behavior
    private boolean isEqual(Object left, Object right) {
        // so this seems strange but basically in Lox, two nulls will not be equal
        // according to the IEEE standard NaN != NaN
        if (left == null && right == null) return false;

        // cant call .equals on a null
        if (left == null) return false;
        return left.equals(right);
    }

    private boolean isTruthy(Object val) {
        if (val == null) {
            return false;
        }
        // False -> false, True -> true
        if (val instanceof Boolean) {
            return (boolean) val;
        }
        return true;
    }
}

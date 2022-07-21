package com.craftinginterpreters.lox;
import static com.craftinginterpreters.lox.TokenType.*;

import java.util.List;

// this marks the beginning of the RUNTIME
// the class that actually takes the expressions from the syntax tree
// and EVALUATES them in Java. Result is always an
// Object (which can be any of the primitive data types)
// this allows for dynamic typing, meaning we only resolve types here.
public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    // the current innermost scope's environment
    private Environment currentEnv = new Environment();

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
        System.out.println(stringify(evaluate(statement.expression)));

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

    @Override
    public Void visitBlockStatementStatement(Statement.BlockStatement statement) {
        // store the old environment since we gotta restore it later
        Environment previousEnv = this.currentEnv;
        
        try {
            // make a new environment for this new block
            Environment blockEnv = new Environment(currentEnv);
            // set current environment to block environment
            this.currentEnv = blockEnv;

            for (Statement s : statement.statements) {
                execute(s);
            }
        }
        finally {
            // restore old environment
            this.currentEnv = previousEnv;
        }
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

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return currentEnv.getVariableValue(expression.name);
    }

    @Override
    public Object visitAssignmentExpression(Expression.Assignment expression) {
        Object rhs = evaluate(expression.value);
        currentEnv.changeExistingVariable(expression.name, rhs);
        return rhs;
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

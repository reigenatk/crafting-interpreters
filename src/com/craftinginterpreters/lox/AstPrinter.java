/*
 * This class pretty prints an AST into a String
 */

package com.craftinginterpreters.lox;

// so to explain this in my own terms, let's say we call AstPrinter.print(e);
// where e is of type Expression. Then this will call exp.accept(this)
// where this is of type Visitor<String>, (passed in via the "this" in the print() function below
// this will go to call one of the 4 "visit" functions, 
// depending on what type Expression is, since
// accept is abstract and is implemented by multiple subclasses
// and since the visit functions are all called from Expression.java with arg "visitor"
// which is of type Visitor<R>, and also since we know we're calling from AstPrinter
// then we know it will call our override functions below, which translates
// non-expressions into strings, and recursively calls accept() on any deeper nested
// Expressions, until they are all translated.

// so in summary its a recursive solution and it also passed both Visitor<String> to 
// Expression, who passes back one of Expression.Binary, Grouping, Literal, or Unary
// back to the visit functions so we know what to print. Phew.

// the abstract syntax tree printer will implement the interface 
public class AstPrinter implements Expression.Visitor<String> {
    String print(Expression exp) {
        return exp.accept(this); // passes obj of type Visitor<String>
    }

    // the following four methods help print out the syntax tree nicely in String format
    // notice that they access the member variables for each of our Expression productions
    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }
    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression);
    }
    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value == null) {
            return "nil";
        }
        else {
            return expression.value.toString();
        }
    }
    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    }

    // a helper function to add parenthesis and spacing nicely
    private String parenthesize(String name, Expression... exprs) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Expression exp : exprs) {
            sb.append(" ");
            // recursively call the function on any sub-expressions!
            sb.append(exp.accept(this));
        }
        sb.append(")");
        return sb.toString(); 
    }

    public static void main(String[] args) {
        Expression exp = new Expression.Binary(new Expression.Literal(32), 
        new Token(TokenType.MINUS, "-", null, 1), 
        new Expression.Grouping(new Expression.Literal(41)));
        
        System.out.println(new AstPrinter().print(exp));
    }
}

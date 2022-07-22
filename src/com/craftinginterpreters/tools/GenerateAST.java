package com.craftinginterpreters.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// a java file to auto generate more java code, which creates classes that represents all 
// the possible productions in the Lox language
public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        // where to put the output java files
        String outputDir = args[0];

        // an expression is a grouping of tokens and other expressions in a certain order
        defineAST(outputDir, "Expression", Arrays.asList(
        "Binary : Expression left, Token operator, Expression right",
        "Grouping : Expression expression",
        "Literal : Object value",
        "Unary : Token operator, Expression right",
        "Variable: Token name",
        "Assignment: Token name, Expression value",
        "Logical: Expression left, Token operator, Expression right"
        ));

        // a statement is a superset of expressions. You can have expressions inside of statements
        defineAST(outputDir, "Statement", Arrays.asList(
            "ExpressionStatement : Expression expression",
            "PrintStatement : Expression expression",
            "VariableDeclaration : Token name, Expression initializer",
            "BlockStatement: List<Statement> statements",
            "IfStatement: Expression condition, Statement ifCode, Statement elseCode",
            "WhileStatement: Expression condition, Statement code"
        ));
    }

    private static void defineAST(String outputDir, String baseName, List<String> productions) throws IOException {
        String path = outputDir + "/" + baseName + ".java";

        // we will use printWriter to write into files
        PrintWriter p = new PrintWriter(path, "UTF-8");

        // this writes the following into our new java file
        p.println("// This file was auto-generated by tools/GenerateAST.java");
        p.println("package com.craftinginterpreters.lox;");
        p.println();
        p.println("import java.util.List;");
        p.println();
        p.println("abstract class " + baseName + " {");
        
        // visitor interface, using Generics. Generics are more or less just 
        // like C++ templates. You can make a generic interface like you see here
        // generates something like 
        /**
        interface Visitor<R> {
            R visitBinaryExpression(Binary expression);
            R visitGroupingExpression(Grouping expression);
            R visitLiteralExpression(Literal expression);
            R visitUnaryExpression(Unary expression);
	    }
        */
        defineVisitor(p, baseName, productions);

        for (String s : productions) {
            String[] splitted = s.split(":");
            String productionName = splitted[0].trim();
            String arguments = splitted[1].trim();
            p.println("\tstatic class " + productionName + " extends " + baseName + " {");

            String[] splittedArgs = arguments.split(", ");
            // instance variables
            for (String field : splittedArgs) {
                p.println("\t\tfinal " + field + ";");
            }

            // Visitor pattern. Example below (in Binary class):
            // here we make a generic method, the <R> passes along the type and 
            // the second "R" is the return value. 
            /**
             * 	@Override
		        <R> R accept(Visitor<R> visitor) {
			    return visitor.visitBinaryExpression(this);
		        }
             */
            p.println();
            p.println("\t\t@Override");
            p.println("\t\t<R> R accept(Visitor<R> visitor) {");
            p.println("\t\t\treturn visitor.visit" + productionName + baseName + "(this);");
            p.println("\t\t}");
            p.println();

            // the constructor
            p.println("\t\t" + productionName + "(" + arguments + ") {");
            
            for (String field : splittedArgs) {
                String fieldName = field.split(" ")[1];
                p.println("\t\t\tthis." + fieldName + " = " + fieldName + ";");
            }
            p.println("\t\t}"); // closes constructor

            p.println("\t}"); // close off production class
        }
        p.println();

        // abstract accept() method
        p.println("\tabstract <R> R accept(Visitor<R> visitor);");

        p.println("}"); // close off abstract class

        p.close();
    }

    private static void defineVisitor(PrintWriter pw, String name, List<String> types) {
        pw.println("\tinterface Visitor<R> {");
        for (String s : types) {
            String[] splitted = s.split(":");
            String productionName = splitted[0].trim();
            pw.println("\t\tR visit" + productionName + name + "(" + productionName + " " 
            + name.toLowerCase() + ");");
        }
        pw.println("\t}");
    }
}

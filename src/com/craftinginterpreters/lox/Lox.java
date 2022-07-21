/**
 * This class is the entry point of our program
 */

package com.craftinginterpreters.lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
    // sets to true if we have error during lexing or parsing
    static boolean hadError = false;

    // sets to true if we have error during runtime
    static boolean hadRuntimeError = false;

    // an interpreter. It has no stuff to initialize in a constructor which is why we can do this
    static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            // if src file provided
            runFile(args[0]);
        }
        else {
            // else run interactive prompt
            runPrompt();
        }
    }

    private static void runFile(String filepath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filepath));
        // create a String from the byte array
        run(new String(bytes, Charset.defaultCharset()));
        System.out.println("The current charset is: " + Charset.defaultCharset().displayName());
        
        // if error parsing or lexing, or during runtime, exit the lox program
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // while loop?
        for (;;) {
            System.out.print("Lox> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            // Error flag exists for each run of the program, and in the interpreter 
            // we rerun the program each time we press enter
            hadError = false;
        }
    }

    // THIS IS THE IMPORTANT STUFF
    private static void run(String input_string) {
        // pass each line to the scanner to be lexed
        Scanner scanner = new Scanner(input_string);
        List<Token> tokens = scanner.scanTokens();

        for (Token t : tokens) {
            System.out.println(t);
        }

        // pass list of tokens to parser
        Parser parser = new Parser(tokens);
        Expression exp = parser.parse();

        // if there was an error on this line, don't print
        if (hadError) return;

        // print out the abstract syntax tree that the parser sees
        System.out.println(new AstPrinter().print(exp));

        // try to evaluate the syntax tree
        String result = interpreter.interpret(exp);

        // check for runtime errors
        if (hadRuntimeError) return;

        System.out.println(result);
    }

    // ===================================== ERROR HANDLERS BEGIN ====================+===== //
    
    // note that this doesn't actually stop the program, it just prints error message instead
    // called from Scanner.java
    static void error(int line, String message) {
        reportError(line, "", message);
    }

    // another interface for error reporting, where we pass along a erroring Token instead
    // called from Parser.java
    static void error(Token t, String message) {
        if (t.type == TokenType.EOF) {
            reportError(t.line, " at end of file", message);
        }
        else {
            reportError(t.line, " at '" + t.lexeme + "'", message);
        }
    }

    // called from Interpreter.java (runtime)
    static void error(RuntimeError r) {
        hadRuntimeError = true;
        System.err.println("[line " + r.token.line + "] token " + r.token.lexeme + ": " + r.getMessage());
    }

    private static void reportError(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    // ===================================== ERROR HANDLERS END ============================ //
}
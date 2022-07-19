package com.craftinginterpreters.lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
    static boolean hadError = false;

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
        if (hadError) System.exit(65);
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

    private static void run(String input_string) {
        Scanner scanner = new Scanner(input_string);
        List<Token> tokens = scanner.scanTokens();

        for (Token t : tokens) {
            System.out.println(t);
        }
    }

    // note that this doesn't actually stop the program, it just prints error message instead
    static void error(int line, String message) {
        reportError(line, "", message);
    }

    private static void reportError(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
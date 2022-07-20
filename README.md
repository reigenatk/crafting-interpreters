## Crafting Interpreters

I decided to learn more about interpreters, compilers, etc. as it seemed like a fun challenge. I'm going to work through [this very well written book](https://github.com/munificent/craftinginterpreters) and hopefully have something cool to show by the end of it.

Some notes:

1. The scanner (formally a lexer) works by scanning an input string token by token

2. It then passes a list of Tokens to the Parser, which will create an Abstract Syntax Tree (AST) as well as detect any syntax errors.


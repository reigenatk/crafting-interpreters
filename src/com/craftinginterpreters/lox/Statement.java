// This file was auto-generated by tools/GenerateAST.java
package com.craftinginterpreters.lox;

import java.util.List;

abstract class Statement {
	interface Visitor<R> {
		R visitExpressionStatementStatement(ExpressionStatement statement);
		R visitPrintStatementStatement(PrintStatement statement);
		R visitVariableDeclarationStatement(VariableDeclaration statement);
		R visitBlockStatementStatement(BlockStatement statement);
		R visitIfStatementStatement(IfStatement statement);
		R visitWhileStatementStatement(WhileStatement statement);
		R visitBreakStatementStatement(BreakStatement statement);
		R visitFunctionStatementStatement(FunctionStatement statement);
	}
	static class ExpressionStatement extends Statement {
		final Expression expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStatementStatement(this);
		}

		ExpressionStatement(Expression expression) {
			this.expression = expression;
		}
	}
	static class PrintStatement extends Statement {
		final Expression expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStatementStatement(this);
		}

		PrintStatement(Expression expression) {
			this.expression = expression;
		}
	}
	static class VariableDeclaration extends Statement {
		final Token name;
		final Expression initializer;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableDeclarationStatement(this);
		}

		VariableDeclaration(Token name, Expression initializer) {
			this.name = name;
			this.initializer = initializer;
		}
	}
	static class BlockStatement extends Statement {
		final List<Statement> statements;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStatementStatement(this);
		}

		BlockStatement(List<Statement> statements) {
			this.statements = statements;
		}
	}
	static class IfStatement extends Statement {
		final Expression condition;
		final Statement ifCode;
		final Statement elseCode;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStatementStatement(this);
		}

		IfStatement(Expression condition, Statement ifCode, Statement elseCode) {
			this.condition = condition;
			this.ifCode = ifCode;
			this.elseCode = elseCode;
		}
	}
	static class WhileStatement extends Statement {
		final Expression condition;
		final Statement code;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStatementStatement(this);
		}

		WhileStatement(Expression condition, Statement code) {
			this.condition = condition;
			this.code = code;
		}
	}
	static class BreakStatement extends Statement {

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStatementStatement(this);
		}

		BreakStatement() {
		}
	}
	static class FunctionStatement extends Statement {
		final Token funcName;
		final List<Token> args;
		final List<Statement> code;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStatementStatement(this);
		}

		FunctionStatement(Token funcName, List<Token> args, List<Statement> code) {
			this.funcName = funcName;
			this.args = args;
			this.code = code;
		}
	}

	abstract <R> R accept(Visitor<R> visitor);
}

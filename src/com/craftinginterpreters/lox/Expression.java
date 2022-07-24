// This file was auto-generated by tools/GenerateAST.java
package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expression {
	interface Visitor<R> {
		R visitBinaryExpression(Binary expression);
		R visitGroupingExpression(Grouping expression);
		R visitLiteralExpression(Literal expression);
		R visitUnaryExpression(Unary expression);
		R visitVariableExpression(Variable expression);
		R visitAssignmentExpression(Assignment expression);
		R visitLogicalExpression(Logical expression);
		R visitCallExpression(Call expression);
		R visitGetExpression(Get expression);
		R visitSetExpression(Set expression);
	}
	static class Binary extends Expression {
		final Expression left;
		final Token operator;
		final Expression right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpression(this);
		}

		Binary(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
	}
	static class Grouping extends Expression {
		final Expression expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpression(this);
		}

		Grouping(Expression expression) {
			this.expression = expression;
		}
	}
	static class Literal extends Expression {
		final Object value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpression(this);
		}

		Literal(Object value) {
			this.value = value;
		}
	}
	static class Unary extends Expression {
		final Token operator;
		final Expression right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpression(this);
		}

		Unary(Token operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}
	}
	static class Variable extends Expression {
		final Token name;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpression(this);
		}

		Variable(Token name) {
			this.name = name;
		}
	}
	static class Assignment extends Expression {
		final Token name;
		final Expression value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignmentExpression(this);
		}

		Assignment(Token name, Expression value) {
			this.name = name;
			this.value = value;
		}
	}
	static class Logical extends Expression {
		final Expression left;
		final Token operator;
		final Expression right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpression(this);
		}

		Logical(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
	}
	static class Call extends Expression {
		final Expression callee;
		final List<Expression> args;
		final Token closingParenthesis;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpression(this);
		}

		Call(Expression callee, List<Expression> args, Token closingParenthesis) {
			this.callee = callee;
			this.args = args;
			this.closingParenthesis = closingParenthesis;
		}
	}
	static class Get extends Expression {
		final Expression object;
		final Token name;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpression(this);
		}

		Get(Expression object, Token name) {
			this.object = object;
			this.name = name;
		}
	}
	static class Set extends Expression {
		final Expression object;
		final Token name;
		final Expression value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpression(this);
		}

		Set(Expression object, Token name, Expression value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}
	}

	abstract <R> R accept(Visitor<R> visitor);
}

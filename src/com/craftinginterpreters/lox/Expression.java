package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expression {
	interface Visitor<R> {
		R visitBinaryExpression(Binary expression);
		R visitGroupingExpression(Grouping expression);
		R visitLiteralExpression(Literal expression);
		R visitUnaryExpression(Unary expression);
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

	abstract <R> R accept(Visitor<R> visitor);
}

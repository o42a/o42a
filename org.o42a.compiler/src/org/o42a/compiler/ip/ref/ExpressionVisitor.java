/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.compiler.ip.ref;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.macro.MacroExpansionStep.expandMacro;
import static org.o42a.compiler.ip.ref.RefInterpreter.integer;
import static org.o42a.compiler.ip.type.macro.TypeConsumer.NO_TYPE_CONSUMER;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.value.ValueType.STRING;

import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.ValueTypeNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.ref.operator.LogicalExpression;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.compiler.ip.type.macro.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;


public final class ExpressionVisitor
		extends AbstractExpressionVisitor<Ref, Distributor> {

	private final Interpreter ip;
	private final Referral referral;
	private final TypeConsumer typeConsumer;

	public ExpressionVisitor(Interpreter ip, Referral referral) {
		this.ip = ip;
		this.referral = referral;
		this.typeConsumer = NO_TYPE_CONSUMER;
	}

	public ExpressionVisitor(
			Interpreter ip,
			Referral referral,
			TypeConsumer typeConsumer) {
		this.ip = ip;
		this.referral = referral;
		this.typeConsumer = typeConsumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public Ref visitDecimal(DecimalNode decimal, Distributor p) {
		return integer(decimal, p);
	}

	@Override
	public Ref visitText(TextNode textNode, Distributor p) {

		final String text = textNode.getText();

		if (textNode.isDoubleQuote()) {
			return STRING.constantRef(location(p, textNode), p, text);
		}

		return super.visitText(textNode, p);
	}

	@Override
	public Ref visitUnary(UnaryNode expression, Distributor p) {

		final ExpressionNode operandNode = expression.getOperand();

		if (operandNode == null) {
			return null;
		}

		switch (expression.getOperator()) {
		case PLUS:
		case MINUS:

			final Phrase phrase = ip().phraseIp().unary(expression, p);

			if (phrase == null) {
				return null;
			}

			return phrase.toRef();
		case IS_TRUE:
		case NOT:
			return new LogicalExpression(
					ip(),
					p.getContext(),
					expression,
					p).toRef();
		case MACRO_EXPANSION:

			final Ref operand = operandNode.accept(ip().targetExVisitor(), p);

			if (operand == null) {
				return null;
			}

			return expandMacro(operand);
		}

		return super.visitUnary(expression, p);
	}

	@Override
	public Ref visitBinary(BinaryNode expression, Distributor p) {
		return ip().phraseIp().binary(expression, p);
	}

	@Override
	public Ref visitBrackets(BracketsNode brackets, Distributor p) {
		return new ArrayConstructor(
				ip(),
				p.getContext(),
				brackets,
				p).toRef();
	}

	@Override
	public Ref visitParentheses(ParenthesesNode parentheses, Distributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	public Ref visitAscendants(AscendantsNode ascendants, Distributor p) {
		if (!ascendants.hasSamples()) {

			final AncestorTypeRef ancestor =
					ip().typeIp().parseAncestor(ascendants, p);

			if (ancestor.isImplied()) {
				return super.visitAscendants(ascendants, p);
			}

			return ancestor.getAncestor().getRef();
		}

		return ip().phraseIp().ascendants(ascendants, p).toRef();
	}

	@Override
	public Ref visitValueType(ValueTypeNode valueType, Distributor p) {

		final Phrase phrase =
				ip().phraseIp().ascendants(valueType, p, this.typeConsumer);

		if (phrase == null) {
			return super.visitValueType(valueType, p);
		}

		return phrase.toRef();
	}

	@Override
	public Ref visitPhrase(PhraseNode phrase, Distributor p) {
		return ip().phraseIp().phrase(phrase, p, this.typeConsumer).toRef();
	}

	@Override
	protected Ref visitRef(RefNode ref, Distributor p) {
		return ref.accept(this.referral.refVisitor(ip()), p);
	}

	@Override
	protected Ref visitExpression(ExpressionNode expression, Distributor p) {
		p.getLogger().invalidExpression(expression);
		return errorRef(location(p, expression), p);
	}

}

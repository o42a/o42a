/*
    Compiler
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.common.macro.Macros.expandMacro;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.ref.RefInterpreter.number;
import static org.o42a.compiler.ip.type.TypeConsumer.EXPRESSION_TYPE_CONSUMER;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;
import static org.o42a.core.ref.Ref.errorRef;
import static org.o42a.core.value.ValueType.STRING;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.ref.keeper.KeepValue;
import org.o42a.compiler.ip.ref.operator.LogicalExpression;
import org.o42a.compiler.ip.ref.operator.ValueOf;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerLogger;
import org.o42a.util.log.LogInfo;


public final class ExpressionVisitor
		extends AbstractExpressionVisitor<Ref, AccessDistributor> {

	private final Interpreter ip;
	private final Referral referral;
	private final TypeConsumer typeConsumer;

	public ExpressionVisitor(Interpreter ip, Referral referral) {
		this.ip = ip;
		this.referral = referral;
		this.typeConsumer = EXPRESSION_TYPE_CONSUMER;
	}

	public ExpressionVisitor(
			Interpreter ip,
			Referral referral,
			TypeConsumer typeConsumer) {
		this.ip = ip;
		this.referral = referral;
		this.typeConsumer =
				typeConsumer == NO_TYPE_CONSUMER
				? EXPRESSION_TYPE_CONSUMER : typeConsumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public Ref visitNumber(NumberNode number, AccessDistributor p) {
		return number(number, p);
	}

	@Override
	public Ref visitText(TextNode textNode, AccessDistributor p) {

		final String text = textNode.getText();

		if (textNode.isDoubleQuoted()) {
			return STRING.constantRef(location(p, textNode), p, text);
		}

		return super.visitText(textNode, p);
	}

	@Override
	public Ref visitAscendants(AscendantsNode ascendants, AccessDistributor p) {
		if (!ascendants.hasSamples()) {

			final AncestorTypeRef ancestor =
					ip().typeIp().parseAncestor(ascendants, p);

			if (ancestor.isImplied()) {
				return super.visitAscendants(ascendants, p);
			}

			return ancestor.getAncestor().getTypeRef().getRef();
		}

		return ip().phraseIp()
				.ascendantsPhrase(ascendants, p, this.typeConsumer)
				.toRef();
	}

	@Override
	public Ref visitTypeParameters(
			TypeParametersNode parameters,
			AccessDistributor p) {

		final PhraseBuilder phrase =
				ip()
				.phraseIp()
				.typeParametersPhrase(parameters, p, this.typeConsumer);

		if (phrase == null) {
			return super.visitTypeParameters(parameters, p);
		}

		return phrase.toRef();
	}

	@Override
	public Ref visitGroup(GroupNode group, AccessDistributor p) {
		return group.getExpression().accept(this, p);
	}

	@Override
	public Ref visitUnary(UnaryNode expression, AccessDistributor p) {
		if (expression.getOperand() == null) {
			return null;
		}

		switch (expression.getOperator()) {
		case PLUS:
		case MINUS:
			return unaryPhrase(expression, p);
		case IS_TRUE:
		case NOT:
			return new LogicalExpression(
					ip(),
					p.getContext(),
					expression,
					p).toRef();
		case VALUE_OF:
			return new ValueOf(ip(), p.getContext(), expression, p).toRef();
		case KEEP_VALUE:
			return keepValue(expression, p);
		case MACRO_EXPANSION:
			return macroExpansion(expression, p);
		}

		return super.visitUnary(expression, p);
	}

	@Override
	public Ref visitBinary(BinaryNode expression, AccessDistributor p) {

		final Ref binary =
				ip().phraseIp().binary(expression, p, this.typeConsumer);

		if (binary != null) {
			return binary;
		}

		return super.visitBinary(expression, p);
	}

	@Override
	public Ref visitBrackets(BracketsNode brackets, AccessDistributor p) {
		return new ArrayConstructor(
				ip(),
				p.getContext(),
				brackets,
				p).toRef();
	}

	@Override
	public Ref visitParentheses(
			ParenthesesNode parentheses,
			AccessDistributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	public Ref visitPhrase(PhraseNode phrase, AccessDistributor p) {

		final PhraseBuilder result =
				ip().phraseIp().phrase(phrase, p, this.typeConsumer);

		if (!this.referral.isBodyReferral()) {
			return result.toRef();
		}

		return result.referBody().toRef();
	}

	@Override
	protected Ref visitRef(RefNode ref, AccessDistributor p) {

		final Owner owner = ref.accept(ip().refIp().ownerVisitor(), p);

		if (owner == null) {
			return null;
		}

		return this.referral.expandIfMacro(owner);
	}

	@Override
	protected Ref visitExpression(ExpressionNode expression, AccessDistributor p) {
		invalidExpression(p.getLogger(), expression);
		return errorRef(location(p, expression), p);
	}

	private static void invalidExpression(
			CompilerLogger logger,
			LogInfo location) {
		logger.error("invalid_expression", location, "Not a valid expression");
	}

	private Ref unaryPhrase(UnaryNode expression, AccessDistributor p) {

		final PhraseBuilder phrase =
				ip().phraseIp().unary(expression, p, this.typeConsumer);

		if (phrase == null) {
			return null;
		}

		return phrase.toRef();
	}

	private Ref keepValue(UnaryNode expression, AccessDistributor p) {

		final Ref value =
				expression.getOperand().accept(ip().targetExVisitor(), p);

		if (value == null) {
			return null;
		}

		return new KeepValue(location(p, expression.getSign()), value).toRef();
	}

	private Ref macroExpansion(UnaryNode expression, AccessDistributor p) {

		final Ref operand =
				expression.getOperand().accept(ip().targetExVisitor(), p);

		if (operand == null) {
			return null;
		}

		return expandMacro(operand);
	}

}

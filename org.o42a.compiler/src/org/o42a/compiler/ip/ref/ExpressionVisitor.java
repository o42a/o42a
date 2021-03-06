/*
    Compiler
    Copyright (C) 2010-2014 Ruslan Lopatin

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
import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;
import static org.o42a.core.value.ValueType.STRING;

import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.phrase.PhraseBuilder;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.compiler.ip.ref.operator.LogicalExpression;
import org.o42a.compiler.ip.ref.operator.ValueOf;
import org.o42a.compiler.ip.ref.owner.Owner;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;
import org.o42a.core.value.link.LinkValueType;


public final class ExpressionVisitor
		implements ExpressionNodeVisitor<Ref, AccessDistributor> {

	private final Interpreter ip;
	private final TypeConsumer typeConsumer;

	public ExpressionVisitor(Interpreter ip) {
		this.ip = ip;
		this.typeConsumer = EXPRESSION_TYPE_CONSUMER;
	}

	public ExpressionVisitor(Interpreter ip, TypeConsumer typeConsumer) {
		this.ip = ip;
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

		return invalidExpression(textNode, p);
	}

	@Override
	public Ref visitTypeArguments(
			TypeArgumentsNode arguments,
			AccessDistributor p) {

		final PhraseBuilder phrase =
				ip()
				.phraseIp()
				.typeArgumentsPhrase(arguments, p, this.typeConsumer);

		if (phrase == null) {
			return invalidExpression(arguments, p);
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
			return new ValueOf(
					ip(),
					p.getContext(),
					expression,
					p).toRef();
		case LINK:
			return link(expression, p, LinkValueType.LINK);
		case VARIABLE:
			return link(expression, p, LinkValueType.VARIABLE);
		}

		return invalidExpression(expression, p);
	}

	@Override
	public Ref visitBinary(BinaryNode expression, AccessDistributor p) {

		final Ref binary =
				ip().phraseIp().binary(expression, p, this.typeConsumer);

		if (binary != null) {
			return binary;
		}

		return invalidExpression(expression, p);
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

		return invalidExpression(parentheses, p);
	}

	@Override
	public Ref visitPhrase(PhraseNode phrase, AccessDistributor p) {

		final PhraseBuilder result =
				ip().phraseIp().phrase(phrase, p, this.typeConsumer);

		return result.toRef();
	}

	@Override
	public Ref visitRef(RefNode ref, AccessDistributor p) {

		final Owner owner = ref.accept(ip().refIp().ownerVisitor(), p);

		if (owner == null) {
			return null;
		}

		final Ref result = owner.ref();

		if (!owner.isMacroExpanding()) {
			return result;
		}

		return expandMacro(result);
	}

	@Override
	public Ref visitExpression(ExpressionNode expression, AccessDistributor p) {
		return invalidExpression(expression, p);
	}

	private Ref unaryPhrase(UnaryNode expression, AccessDistributor p) {

		final PhraseBuilder phrase =
				ip().phraseIp().unary(expression, p, this.typeConsumer);

		if (phrase == null) {
			return null;
		}

		return phrase.toRef();
	}

	private Ref link(
			UnaryNode expression,
			AccessDistributor p,
			LinkValueType linkType) {

		final Ref value =
				expression.getOperand().accept(ip().expressionVisitor(), p);

		if (value == null) {
			return null;
		}

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				new Location(p.getContext(), expression),
				p,
				this.typeConsumer);

		phrase.setAncestor(linkType.typeRef(
				new Location(p.getContext(), expression.getSign()),
				p.getScope()));
		phrase.setTypeParameters(
				linkType.typeParameters(value.getInterface())
				.toObjectTypeParameters());
		phrase.phrase().declarations(valueBlock(value));

		return phrase.toRef();
	}

	private Ref invalidExpression(
			ExpressionNode expression,
			AccessDistributor p) {
		p.getLogger().error(
				"invalid_expression",
				expression,
				"Not a valid expression");
		return errorRef(location(p, expression), p);
	}

}

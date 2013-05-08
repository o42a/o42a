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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;

import org.o42a.ast.expression.*;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.common.ref.cmp.ComparisonExpression;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.ref.Ref;


public final class PhraseInterpreter {

	private final Interpreter ip;

	public PhraseInterpreter(Interpreter ip) {
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public PhraseBuilder phrase(
			PhraseNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder prefixed =
				phraseWithPrefix(node, distributor, typeConsumer);

		if (prefixed == null) {
			return null;
		}

		return prefixed.addParts(node);
	}

	public PhraseBuilder ascendantsPhrase(
			AscendantsNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.prefixByAscendants(node).noDeclarations();
	}

	public PhraseBuilder typeParametersPhrase(
			TypeParametersNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.prefixByTypeParameters(node).noDeclarations();
	}

	public PhraseBuilder typeArgumentsPhrase(
			TypeArgumentsNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.prefixByTypeArguments(node).noDeclarations();
	}

	public PhraseBuilder unary(
			UnaryNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.unary(node);
	}

	public Ref binary(
			BinaryNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final BinaryOperator operator = node.getOperator();

		switch (operator) {
		case COMPARE:
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			return binaryPhrase(node, distributor, typeConsumer).toRef();
		case GREATER:
			return comparison(node, distributor, BinaryPhraseOperator.GREATER);
		case GREATER_OR_EQUAL:
			return comparison(
					node,
					distributor,
					BinaryPhraseOperator.GREATER_OR_EQUAL);
		case LESS:
			return comparison(node, distributor, BinaryPhraseOperator.LESS);
		case LESS_OR_EQUAL:
			return comparison(
					node,
					distributor,
					BinaryPhraseOperator.LESS_OR_EQUAL);
		case NOT_EQUAL:
			return comparison(
					node,
					distributor,
					BinaryPhraseOperator.NOT_EQUALS);
		case EQUAL:
			return comparison(node, distributor, BinaryPhraseOperator.EQUALS);
		case SUFFIX:
			return suffixPhrase(node, distributor, typeConsumer).toRef();
		}

		return null;
	}

	final PhraseBuilder phraseWithPrefix(
			PhraseNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return node.getPrefix().accept(PHRASE_PREFIX_VISITOR, phrase);
	}

	private PhraseBuilder binaryPhrase(
			BinaryNode node,
			AccessDistributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.binary(node);
	}

	private Ref comparison(
			BinaryNode node,
			AccessDistributor distributor,
			BinaryPhraseOperator operator) {

		final Ref left = node.getLeftOperand().accept(
				ip().expressionVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right =
				rightOperand.accept(ip().expressionVisitor(), distributor);

		if (right == null) {
			return null;
		}

		return new ComparisonExpression(
				location(distributor, node),
				operator,
				left,
				right).toRef();
	}

	private PhraseBuilder suffixPhrase(
			BinaryNode node,
			AccessDistributor distributor,
			TypeConsumer consumer) {

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		return rightOperand.accept(
				new SuffixVisitor(this, consumer, node),
				distributor);
	}

}

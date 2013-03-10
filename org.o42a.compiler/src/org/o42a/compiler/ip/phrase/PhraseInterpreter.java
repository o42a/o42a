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
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.operator.ComparisonExpression;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
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
			Distributor distributor,
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
			Distributor distributor,
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
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.prefixByTypeParameters(node).noDeclarations();
	}

	public PhraseBuilder unary(
			UnaryNode node,
			Distributor distributor,
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
			Distributor distributor,
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
		case GREATER_OR_EQUAL:
		case LESS:
		case LESS_OR_EQUAL:
		case NOT_EQUAL:
		case EQUAL:
			return new ComparisonExpression(
					ip(),
					node,
					distributor,
					typeConsumer)
			.toRef();
		case SUFFIX:
			return suffixPhrase(node, distributor, typeConsumer).toRef();
		}

		return null;
	}

	final PhraseBuilder phraseWithPrefix(
			PhraseNode node,
			Distributor distributor,
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
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return phrase.binary(node);
	}

	private PhraseBuilder suffixPhrase(
			BinaryNode node,
			Distributor distributor,
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

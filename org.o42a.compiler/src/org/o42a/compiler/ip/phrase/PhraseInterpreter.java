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
import static org.o42a.compiler.ip.phrase.PhrasePartVisitor.PHRASE_PART_VISITOR;
import static org.o42a.compiler.ip.phrase.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.compiler.ip.phrase.part.SuffixedByPhrase;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.ref.operator.ComparisonExpression;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.compiler.ip.type.ascendant.SampleSpecVisitor;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRefParameters;


public final class PhraseInterpreter {

	private final Interpreter ip;

	public PhraseInterpreter(Interpreter ip) {
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public Phrase phrase(
			PhraseNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase prefixed =
				phraseWithPrefix(node, distributor, typeConsumer);

		if (prefixed == null) {
			return null;
		}

		return addParts(prefixed, node);
	}

	public Phrase ascendantsPhrase(
			AscendantsNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase phrase = new Phrase(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);
		final Phrase prefixed = prefixByAscendants(phrase, node);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public Phrase typeParametersPhrase(
			TypeParametersNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase phrase = new Phrase(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);
		final Phrase prefixed = prefixByTypeParameters(phrase, node);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public Phrase unary(
			UnaryNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase phrase = new Phrase(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);
		final Ref operand = node.getOperand().accept(
				ip().targetExVisitor(phrase.getTypeConsumer().noConsumption()),
				distributor);

		if (operand == null) {
			return null;
		}

		return phrase.setAncestor(operand.toTypeRef()).unary(node).getPhrase();
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
			return binaryPhrase(node, distributor, typeConsumer)
					.getPhrase()
					.toRef();
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
			return suffixPhrase(node, distributor, typeConsumer)
					.getPhrase()
					.toRef();
		}

		return null;
	}

	public BinaryPhrasePart binaryPhrase(
			BinaryNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Ref left = node.getLeftOperand().accept(
				ip().targetExVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		final Phrase phrase = new Phrase(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer)
				.setAncestor(left.toTypeRef());

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right =
				rightOperand.accept(ip().targetExVisitor(), distributor);

		if (right == null) {
			return null;
		}

		return phrase.binary(node, right);
	}

	final Phrase phraseWithPrefix(
			PhraseNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase phrase = new Phrase(
				ip(),
				location(distributor, node),
				distributor,
				typeConsumer);

		return node.getPrefix().accept(PHRASE_PREFIX_VISITOR, phrase);
	}

	static Phrase addParts(Phrase phrase, PhraseNode node) {

		Phrase result = phrase;

		for (PhrasePartNode part : node.getParts()) {
			result = part.accept(PHRASE_PART_VISITOR, result);
		}

		return result;
	}

	static Phrase prefixByAscendants(Phrase phrase, AscendantsNode node) {

		final Distributor distributor = phrase.distribute();
		final AncestorTypeRef ancestor =
				phrase.ip().typeIp().parseAncestor(node, distributor);
		Phrase result = phrase;

		if (ancestor.isImplied()) {
			result = result.setImpliedAncestor(
					location(phrase, node.getAncestor()));
		} else {
			if (ancestor.isBodyReferred()) {
				result = result.referBody();
			}
			result = ancestor.applyTo(result);
		}

		if (!node.hasSamples()) {
			return result;
		}

		final SampleSpecVisitor sampleSpecVisitor =
				new SampleSpecVisitor(phrase.ip());

		for (AscendantNode sampleNode : node.getSamples()) {

			final RefNode specNode = sampleNode.getSpec();

			if (specNode != null) {

				final StaticTypeRef sample =
						specNode.accept(sampleSpecVisitor, distributor);

				if (sample != null) {
					result = result.addSamples(sample);
				}
			}
		}

		return result;
	}

	static Phrase prefixByTypeParameters(
			Phrase phrase,
			TypeParametersNode node) {
		phrase.referBody();

		final TypeRefParameters typeParams =
				phrase.ip().typeIp().typeParameters(
						node.getParameters(),
						phrase.distribute(),
						phrase.getTypeConsumer());
		final TypeNode ascendantNode = node.getType();

		if (ascendantNode == null) {
			return phrase.setImpliedAncestor(location(phrase, node))
					.setTypeParameters(typeParams.toObjectTypeParameters());
		}

		return ascendantNode.accept(
				new PhrasePrefixVisitor(typeParams),
				phrase);
	}

	private SuffixedByPhrase suffixPhrase(
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

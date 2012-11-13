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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.PhrasePartVisitor.PHRASE_PART_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.common.ref.ArbitraryTypeParameters;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.ref.operator.ComparisonExpression;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.compiler.ip.type.ascendant.SampleSpecVisitor;
import org.o42a.compiler.ip.type.macro.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


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

		final Phrase phrase =
				new Phrase(ip(), location(distributor, node), distributor);
		final Phrase prefixed = node.getPrefix().accept(
				new PhrasePrefixVisitor(typeConsumer),
				phrase);

		return addClauses(prefixed, node);
	}

	public Phrase ascendantsPhrase(
			AscendantsNode node,
			Distributor distributor) {

		final Phrase phrase =
				new Phrase(ip(), location(distributor, node), distributor);
		final Phrase prefixed = prefixByAscendants(phrase, node);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public Phrase valueTypePhrase(
			TypeParametersNode node,
			Distributor distributor,
			TypeConsumer typeConsumer) {

		final Phrase phrase =
				new Phrase(ip(), location(distributor, node), distributor);
		final Phrase prefixed = prefixByValueType(phrase, node, typeConsumer);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public Phrase unary(UnaryNode node, Distributor distributor) {

		final Ref operand = node.getOperand().accept(
				ip().targetExVisitor(),
				distributor);

		if (operand == null) {
			return null;
		}

		final Phrase phrase =
				new Phrase(ip(), location(distributor, node), distributor);

		return phrase.setAncestor(operand.toTypeRef()).unary(node).getPhrase();
	}

	public Ref binary(BinaryNode node, Distributor distributor) {
		if (node.getOperator().isArithmetic()) {
			return binaryPhrase(node, distributor).getPhrase().toRef();
		}
		return new ComparisonExpression(ip(), node, distributor).toRef();
	}

	public BinaryPhrasePart binaryPhrase(
			BinaryNode node,
			Distributor distributor) {

		final Ref left = node.getLeftOperand().accept(
				ip().targetExVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		final Phrase phrase =
				new Phrase(ip(), location(distributor, node), distributor);
		final BinaryPhrasePart binary =
				phrase.setAncestor(left.toTypeRef()).binary(node);

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right =
				rightOperand.accept(ip().targetExVisitor(), distributor);

		if (right == null) {
			return null;
		}

		phrase.operand(right);

		return binary;
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
			result = result.setAncestor(ancestor.getAncestor());
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

	static Phrase prefixByValueType(
			Phrase phrase,
			TypeParametersNode node,
			TypeConsumer typeConsumer) {
		phrase.referBody();

		final ArbitraryTypeParameters typeParams =
				phrase.ip().typeIp().typeParameters(
						node.getParameters(),
						phrase.distribute(),
						typeConsumer);
		final TypeNode ascendantNode = node.getType();

		if (ascendantNode == null) {
			return phrase.setImpliedAncestor(location(phrase, node))
					.setTypeParameters(typeParams);
		}

		return ascendantNode.accept(
				new PhrasePrefixVisitor(typeParams),
				phrase);
	}

	private static Phrase addClauses(Phrase phrase, PhraseNode node) {

		Phrase result = phrase;

		for (PhrasePartNode clause : node.getClauses()) {
			result = clause.accept(PHRASE_PART_VISITOR, result);
		}

		return result;
	}

}

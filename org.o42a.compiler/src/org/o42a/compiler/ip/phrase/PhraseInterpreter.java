/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import static org.o42a.compiler.ip.AncestorVisitor.impliedAncestor;
import static org.o42a.compiler.ip.AncestorVisitor.noAncestor;
import static org.o42a.compiler.ip.AncestorVisitor.parseAncestor;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.phrase.ClauseVisitor.CLAUSE_VISITOR;
import static org.o42a.compiler.ip.phrase.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.operator.ComparisonRef;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


public final class PhraseInterpreter {

	public static Phrase phrase(
			Interpreter ip,
			PhraseNode node,
			Distributor distributor) {

		final Phrase phrase =
				new Phrase(ip, location(distributor, node), distributor);
		final Phrase prefixed =
				node.getPrefix().accept(PHRASE_PREFIX_VISITOR, phrase);

		return addClauses(prefixed, node);
	}

	public static Phrase ascendants(
			Interpreter ip,
			AscendantsNode node,
			Distributor distributor) {

		final Phrase phrase =
				new Phrase(ip, location(distributor, node), distributor);
		final Phrase prefixed = prefix(phrase, node);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public static Phrase unary(
			Interpreter ip,
			UnaryNode node,
			Distributor distributor) {

		final Ref operand =
				node.getOperand().accept(ip.expressionVisitor(), distributor);

		if (operand == null) {
			return null;
		}

		final Phrase phrase =
				new Phrase(ip, location(distributor, node), distributor);

		return phrase.setAncestor(operand.toTypeRef()).unary(node).getPhrase();
	}

	public static Ref binary(
			Interpreter ip,
			BinaryNode node,
			Distributor distributor) {
		if (node.getOperator().isArithmetic()) {
			return binaryPhrase(ip, node, distributor).getPhrase().toRef();
		}
		return new ComparisonRef(ip, node, distributor);
	}

	public static BinaryPhrasePart binaryPhrase(
			Interpreter ip,
			BinaryNode node,
			Distributor distributor) {

		final Ref left = node.getLeftOperand().accept(
				ip.expressionVisitor(),
				distributor);

		if (left == null) {
			return null;
		}

		final Phrase phrase =
				new Phrase(ip, location(distributor, node), distributor);
		final BinaryPhrasePart binary =
				phrase.setAncestor(left.toTypeRef()).binary(node);

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right =
				rightOperand.accept(ip.expressionVisitor(), distributor);

		if (right == null) {
			return null;
		}

		phrase.operand(right);

		return binary;
	}

	static Phrase prefix(Phrase phrase, AscendantsNode node) {

		final Distributor distributor = phrase.distribute();
		final AscendantNode[] ascendantNodes = node.getAscendants();
		final TypeRef ancestor = parseAncestor(phrase.ip(), node, distributor);
		final int samplesFrom;
		Phrase result;

		if (ancestor == noAncestor(distributor.getContext())) {
			samplesFrom = 0;
			result = phrase.setImpliedAncestor(
					location(phrase, ascendantNodes[0]));
		} else {
			samplesFrom = 1;
			if (ancestor == impliedAncestor(distributor.getContext())) {
				result = phrase.setImpliedAncestor(
						location(phrase, ascendantNodes[0]));
			} else {
				result = phrase.setAncestor(ancestor);
			}
		}

		for (int i = samplesFrom; i < ascendantNodes.length; ++i) {

			final RefNode sampleNode = ascendantNodes[i].getAscendant();

			if (sampleNode != null) {

				final Ref sampleRef = sampleNode.accept(
						phrase.ip().refVisitor(),
						distributor);

				if (sampleRef != null) {
					result = result.addSamples(sampleRef.toStaticTypeRef());
				}
			}
		}

		return result;
	}

	private static Phrase addClauses(Phrase phrase, PhraseNode node) {

		Phrase result = phrase;

		for (ClauseNode clause : node.getClauses()) {
			result = clause.accept(CLAUSE_VISITOR, result);
		}

		return result;
	}

	private PhraseInterpreter() {
	}

}

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
import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.compiler.ip.phrase.ClauseVisitor.CLAUSE_VISITOR;
import static org.o42a.compiler.ip.phrase.PhrasePrefixVisitor.PHRASE_PREFIX_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.operator.*;
import org.o42a.compiler.ip.phrase.part.BinaryPhrasePart;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


public final class PhraseInterpreter {

	public static Phrase phrase(PhraseNode node, Distributor distributor) {

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);
		final Phrase prefixed =
			node.getPrefix().accept(PHRASE_PREFIX_VISITOR, phrase);

		return addClauses(prefixed, node);
	}

	public static Phrase ascendants(
			AscendantsNode node,
			Distributor distributor) {

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);
		final Phrase prefixed = prefix(phrase, node);

		return prefixed.declarations(emptyBlock(phrase)).getPhrase();
	}

	public static Phrase unary(UnaryNode node, Distributor distributor) {

		final Ref operand =
			node.getOperand().accept(EXPRESSION_VISITOR, distributor);

		if (operand == null) {
			return null;
		}

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);

		return phrase.setAncestor(operand.toTypeRef()).unary(node).getPhrase();
	}

	public static Ref binary(BinaryNode node, Distributor distributor) {

		final Ref left =
			node.getLeftOperand().accept(EXPRESSION_VISITOR, distributor);

		if (left == null) {
			return null;
		}

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);
		final BinaryPhrasePart binary =
			phrase.setAncestor(left.toTypeRef()).binary(node);

		final ExpressionNode rightOperand = node.getRightOperand();

		if (rightOperand == null) {
			return null;
		}

		final Ref right = rightOperand.accept(EXPRESSION_VISITOR, distributor);

		if (right == null) {
			return null;
		}

		phrase.operand(right);

		switch (node.getOperator()) {
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			return phrase.toRef();
		case EQUAL:
			return new EqualsWrap(binary);
		case NOT_EQUAL:
			return new NotEqualsWrap(binary);
		case GREATER:
			return new GreaterRef(phrase);
		case GREATER_OR_EQUAL:
			return new GreaterOrEqualRef(phrase);
		case LESS:
			return new LessRef(phrase);
		case LESS_OR_EQUAL:
			return new LessOrEqualRef(phrase);
		}

		distributor.getLogger().error(
				"unsupported_binary",
				node.getSign(),
				"Binary operator '%s' is not supported",
				node.getOperator().getSign());

		return null;
	}

	static Phrase prefix(Phrase phrase, AscendantsNode node) {

		final Distributor distributor = phrase.distribute();
		final AscendantNode[] ascendantNodes = node.getAscendants();
		final TypeRef ancestor = parseAncestor(node, distributor);
		final int samplesFrom;

		if (ancestor == noAncestor(distributor.getContext())) {
			samplesFrom = 0;
			phrase = phrase.setImpliedAncestor(
					location(phrase, ascendantNodes[0]));
		} else {
			samplesFrom = 1;
			if (ancestor == impliedAncestor(distributor.getContext())) {
				phrase = phrase.setImpliedAncestor(
						location(phrase, ascendantNodes[0]));
			} else {
				phrase = phrase.setAncestor(ancestor);
			}
		}

		for (int i = samplesFrom; i < ascendantNodes.length; ++i) {

			final RefNode sampleNode = ascendantNodes[i].getAscendant();

			if (sampleNode != null) {

				final Ref sampleRef =
					sampleNode.accept(REF_VISITOR, distributor);

				if (sampleRef != null) {
					phrase = phrase.addSamples(sampleRef.toStaticTypeRef());
				}
			}
		}

		return phrase;
	}

	private static Phrase addClauses(Phrase phrase, PhraseNode node) {
		for (ClauseNode clause : node.getClauses()) {
			phrase = clause.accept(CLAUSE_VISITOR, phrase);
		}
		return phrase;
	}

	private PhraseInterpreter() {
	}

}

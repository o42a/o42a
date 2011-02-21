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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorVisitor.*;
import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.contentBuilder;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.phrase.Phrase;
import org.o42a.core.ref.type.TypeRef;


final class PhraseInterpreter {

	private static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
		new PhrasePrefixVisitor();

	private static final ClauseVisitor CLAUSE_VISITOR = new ClauseVisitor();

	private static final ArgumentVisitor ARGUMENT_VISITOR =
		new ArgumentVisitor();

	public static Phrase phrase(PhraseNode node, Distributor distributor) {

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);
		final Phrase prefixed =
			node.getPrefix().accept(PHRASE_PREFIX_VISITOR, phrase);

		return addClauses(prefixed, node);
	}

	public static Phrase phrase(AscendantsNode node, Distributor distributor) {

		final Phrase phrase =
			new Phrase(location(distributor, node), distributor);
		final Phrase prefixed = PhraseInterpreter.prefix(phrase, node);

		return prefixed.declarations(emptyBlock(phrase));
	}

	private static Phrase prefix(Phrase phrase, AscendantsNode node) {

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

	private static Phrase prefix(Phrase phrase, ExpressionNode node) {

		final Distributor distributor = phrase.distribute();
		final TypeRef ancestor = node.accept(ANCESTOR_VISITOR, distributor);

		if (ancestor == null
				|| ancestor == impliedAncestor(distributor.getContext())) {
			return phrase.setImpliedAncestor(location(phrase, node));
		}
		if (ancestor == noAncestor(distributor.getContext())) {

			final Ref sampleRef = node.accept(EXPRESSION_VISITOR, distributor);

			if (sampleRef == null) {
				return phrase.setImpliedAncestor(location(phrase, node));
			}

			return phrase.addSamples(sampleRef.toStaticTypeRef());
		}

		return phrase.setAncestor(ancestor);
	}

	private static Phrase addClauses(Phrase phrase, PhraseNode node) {
		for (ClauseNode clause : node.getClauses()) {
			phrase = clause.accept(CLAUSE_VISITOR, phrase);
		}
		return phrase;
	}

	private PhraseInterpreter() {
	}

	private static final class ClauseVisitor
			extends AbstractClauseVisitor<Phrase, Phrase> {

		@Override
		public Phrase visitName(NameNode name, Phrase p) {
			return p.name(location(p, name), name.getName());
		}

		@Override
		public Phrase visitBraces(BracesNode braces, Phrase p) {
			return p.imperative(contentBuilder(
					new StatementVisitor(p.getContext()),
					braces));
		}

		@Override
		public Phrase visitParentheses(ParenthesesNode parentheses, Phrase p) {
			return p.declarations(contentBuilder(
					new StatementVisitor(p.getContext()),
					parentheses));
		}

		@Override
		public Phrase visitBrackets(BracketsNode brackets, Phrase p) {

			final ArgumentNode[] arguments = brackets.getArguments();

			if (arguments.length == 0) {
				return p.emptyArgument(location(p, brackets));
			}

			for (ArgumentNode arg : arguments) {

				final ExpressionNode value = arg.getValue();

				if (value != null) {
					p = value.accept(ARGUMENT_VISITOR, p);
					continue;
				}
				if (arguments.length == 1) {
					return p.emptyArgument(location(p, brackets));
				}
				p = p.emptyArgument(location(p, arg));
			}

			return p;
		}

		@Override
		public Phrase visitText(TextNode text, Phrase p) {
			if (!text.isDoubleQuote()) {
				return p.string(location(p, text), text.getText());
			}

			final Ref value =
				text.accept(EXPRESSION_VISITOR, p.distribute());

			if (value != null) {
				return p.argument(value);
			}

			return p.emptyArgument(location(p, text));
		}

		@Override
		protected Phrase visitClause(ClauseNode clause, Phrase p) {
			p.getLogger().invalidClause(clause);
			return p;
		}

	}

	private static final class ArgumentVisitor
			extends AbstractExpressionVisitor<Phrase, Phrase> {

		@Override
		public Phrase visitText(TextNode text, Phrase p) {
			if (text.isDoubleQuote()) {
				return super.visitText(text, p);
			}
			return p.string(location(p, text), text.getText());
		}

		@Override
		protected Phrase visitExpression(ExpressionNode expression, Phrase p) {

			final Ref value =
				expression.accept(EXPRESSION_VISITOR, p.distribute());

			if (value != null) {
				return p.argument(value);
			}

			return p.emptyArgument(location(p, expression));
		}

	}

	private static final class PhrasePrefixVisitor
			extends AbstractExpressionVisitor<Phrase, Phrase> {

		@Override
		public Phrase visitAscendants(AscendantsNode ascendants, Phrase p) {
			return prefix(p, ascendants);
		}

		@Override
		protected Phrase visitExpression(ExpressionNode expression, Phrase p) {
			return prefix(p, expression);
		}

	}

}

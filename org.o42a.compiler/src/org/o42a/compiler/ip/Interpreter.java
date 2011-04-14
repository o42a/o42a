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

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.TypeVisitor.TYPE_VISITOR;
import static org.o42a.compiler.ip.UnwrapVisitor.UNWRAP_VISITOR;

import org.o42a.ast.Node;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.TypeNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.phrase.PhraseInterpreter;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.*;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.*;


public class Interpreter {

	public static BlockBuilder contentBuilder(
			StatementVisitor statementVisitor,
			BlockNode<?> node) {
		return new ContentBuilder(statementVisitor, node);
	}

	public static Phrase phrase(PhraseNode node, Distributor distributor) {
		return PhraseInterpreter.phrase(node, distributor);
	}

	public static Phrase phrase(AscendantsNode node, Distributor distributor) {
		return PhraseInterpreter.phrase(node, distributor);
	}

	public static ArrayInitializer arrayInitializer(
			CompilerContext context,
			ArrayNode node,
			FieldDeclaration declaration) {

		final TypeRef itemType;
		final TypeNode itemTypeNode = node.getItemType();

		if (itemTypeNode == null) {
			itemType = null;
		} else {
			itemType =
				itemTypeNode.accept(TYPE_VISITOR, declaration.distribute());
		}

		return arrayInitializer(
				context,
				node,
				itemType,
				node.getItems(),
				declaration);
	}

	public static ArrayInitializer arrayInitializer(
			CompilerContext context,
			BracketsNode node,
			FieldDeclaration declaration) {
		return arrayInitializer(context, node, null, node, declaration);
	}

	public static void addContent(
			StatementVisitor statementVisitor,
			Block<?> block,
			BlockNode<?> blockNode) {

		final SentenceNode[] content = blockNode.getContent();

		for (SentenceNode sentence : content) {
			addSentence(statementVisitor, block, sentence, sentence.getMark());
		}
	}

	public static Location location(ScopeInfo p, Node node) {
		return new Location(p.getContext(), node);
	}

	public static ExpressionNode unwrap(BlockNode<?> block) {

		final SentenceNode[] content = block.getContent();

		if (content.length != 1) {
			return null;
		}

		final SentenceNode sentence = content[0];

		if (sentence.getMark() != null) {
			return null;
		}

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		if (disjunction.length != 1) {
			return null;
		}

		final SerialNode[] conjunction = disjunction[0].getConjunction();

		if (conjunction.length != 1) {
			return null;
		}

		return conjunction[0].getStatement().accept(UNWRAP_VISITOR, null);
	}

	private static ArrayInitializer arrayInitializer(
			CompilerContext context,
			Node node,
			TypeRef itemType,
			BracketsNode brackets,
			FieldDeclaration declaration) {

		final Distributor distributor = declaration.distribute();
		boolean ok = true;
		final ArgumentNode[] arguments = brackets.getArguments();
		final Ref[] items = new Ref[arguments.length];

		for (int i = 0; i < arguments.length; ++i) {

			final ExpressionNode itemNode = arguments[i].getValue();

			if (itemNode == null) {
				ok = false;
				continue;
			}

			final Ref item = itemNode.accept(EXPRESSION_VISITOR, distributor);

			if (item == null) {
				ok = false;
				continue;
			}

			items[i] = item;
		}

		if (!ok) {
			return null;
		}

		return ArrayInitializer.arrayInitializer(
				context,
				node,
				declaration.distribute(),
				itemType,
				items);
	}

	private static Sentence<?> addSentence(
			StatementVisitor statementVisitor,
			Block<?> block,
			SentenceNode node,
			SignNode<SentenceType> mark) {

		final SentenceType type =
			mark != null ? mark.getType() : SentenceType.PROPOSITION;
		final Location location =
			new Location(statementVisitor.getContext(), node);
		final Sentence<?> sentence;

		switch (type) {
		case PROPOSITION:
			sentence = block.propose(location);
			break;
		case CLAIM:
			sentence = block.claim(location);
			break;
		case ISSUE:
			sentence = block.issue(location);
			break;
		default:
			block.getLogger().invalidExpression(node);
			return null;
		}

		fillSentence(statementVisitor, sentence, node);

		return sentence;
	}

	private static void fillSentence(
			final StatementVisitor statementVisitor,
			final Sentence<?> sentence,
			final SentenceNode node) {
		for (AlternativeNode alt : node.getDisjunction()) {

			final Statements<?> alternative = sentence.alternative(
					new Location(statementVisitor.getContext(), alt),
					alt.isOpposite());

			for (SerialNode stat : alt.getConjunction()) {

				final StatementNode st = stat.getStatement();

				if (st != null) {
					st.accept(statementVisitor, alternative);
				}
			}
		}
	}

	private Interpreter() {
	}

	private static final class ContentBuilder extends BlockBuilder {

		private final StatementVisitor statementVisitor;
		private final BlockNode<?> block;

		ContentBuilder(StatementVisitor statementVisitor, BlockNode<?> block) {
			super(statementVisitor.getContext(), block);
			this.statementVisitor = statementVisitor;
			this.block = block;
		}

		@Override
		public void buildBlock(Block<?> block) {
			addContent(this.statementVisitor, block, this.block);
		}

		@Override
		public String toString() {
			return this.block.printContent();
		}

	}

}

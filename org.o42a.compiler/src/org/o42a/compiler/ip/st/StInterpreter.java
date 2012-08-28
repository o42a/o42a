/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.st;

import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;


public final class StInterpreter {

	public static BlockBuilder contentBuilder(
			StatementVisitor statementVisitor,
			BlockNode<?> node) {
		return new ContentBuilder(statementVisitor, node);
	}

	public static void addContent(
			StatementVisitor statementVisitor,
			Block<?, ?> block,
			BlockNode<?> blockNode) {
		for (SentenceNode sentence : blockNode.getContent()) {
			addSentence(statementVisitor, block, sentence, sentence.getType());
		}
	}

	public static Sentence<?, ?> addSentence(
			StatementVisitor statementVisitor,
			Block<?, ?> block,
			SentenceNode node,
			SentenceType type) {

		final Location location =
				new Location(statementVisitor.getContext(), node);
		final Sentence<?, ?> sentence;

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
			throw new IllegalStateException(
					"Unsupported sentence type: " + type);
		}

		if (sentence != null) {
			fillSentence(statementVisitor, sentence, node);
		}

		return sentence;
	}

	private static void fillSentence(
			final StatementVisitor statementVisitor,
			final Sentence<?, ?> sentence,
			final SentenceNode node) {

		int i = 0;
		final AlternativeNode[] disjunction = node.getDisjunction();

		while (i < disjunction.length) {

			final int next = i + 1;
			final AlternativeNode altNode = disjunction[i];
			final Location location =
					new Location(statementVisitor.getContext(), altNode);
			final Statements<?, ?> alt =
					sentence.alternative(location);

			if (alt != null) {
				fillStatements(statementVisitor, altNode, alt);
			}
			i = next;
		}
	}

	private static void fillStatements(
			final StatementVisitor statementVisitor,
			final AlternativeNode altNode,
			final Statements<?, ?> alt) {
		for (SerialNode stat : altNode.getConjunction()) {

			final StatementNode st = stat.getStatement();

			if (st != null) {
				st.accept(statementVisitor, alt);
			}
		}
	}

	private StInterpreter() {
	}

}

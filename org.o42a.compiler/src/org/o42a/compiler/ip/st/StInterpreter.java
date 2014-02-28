/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.ast.sentence.SentenceType.CONTINUED_INTERROGATION;

import java.util.List;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.sentence.*;
import org.o42a.ast.statement.StatementNode;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;
import org.o42a.util.string.Name;


public final class StInterpreter {

	public static BlockBuilder contentBuilder(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			BlockNode<?> node) {
		return new ContentBuilder(accessRules, statementVisitor, node);
	}

	public static void addContent(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			Block<?> block,
			BlockNode<?> blockNode) {
		for (SentenceNode sentence : blockNode.getContent()) {
			addSentence(
					accessRules,
					statementVisitor,
					block,
					sentence,
					sentence.getType());
		}
	}

	public static void addSentence(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			Block<?> block,
			SentenceNode node,
			SentenceType type) {

		final Location location =
				new Location(statementVisitor.getContext(), node);
		final Sentence<?> sentence;

		switch (type) {
		case DECLARATION:
		case CONTINUATION:
			sentence = block.declare(location);
			break;
		case EXCLAMATION:
		case CONTINUED_EXCLAMATION:
			sentence = block.exit(location);
			break;
		case INTERROGATION:
		case CONTINUED_INTERROGATION:
			sentence = block.interrogate(location);
			break;
		default:
			throw new IllegalStateException(
					"Unsupported sentence type: " + type);
		}

		if (sentence == null) {
			return;
		}

		fillSentence(accessRules, statementVisitor, sentence, node);
		addContinuation(block, sentence, node, type);
	}

	private static void fillSentence(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			Sentence<?> sentence,
			SentenceNode node) {

		int i = 0;
		final AlternativeNode[] disjunction = node.getDisjunction();

		while (i < disjunction.length) {

			final int next = i + 1;
			final AlternativeNode altNode = disjunction[i];
			final Location location =
					new Location(statementVisitor.getContext(), altNode);
			final Statements<?> alt = sentence.alternative(location);

			if (alt != null) {
				fillStatements(accessRules, statementVisitor, altNode, alt);
			}
			i = next;
		}
	}

	private static void fillStatements(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			AlternativeNode altNode,
			Statements<?> alt) {
		for (SerialNode stat : altNode.getConjunction()) {

			final StatementNode st = stat.getStatement();

			if (st != null) {
				st.accept(statementVisitor, accessRules.statements(alt));
			}
		}
	}

	private static void addContinuation(
			Block<?> block,
			Sentence<?> sentence,
			SentenceNode node,
			SentenceType type) {

		final Location location = sentence.getLocation();

		if (!type.supportsContinuation()) {
			return;
		}

		final Name label;
		final Location continuationLocation;
		final ContinuationNode continuationNode = node.getContinuation();

		if (continuationNode != null) {
			continuationLocation =
					new Location(location.getContext(), continuationNode);

			final NameNode labelNode = continuationNode.getLabel();

			label = labelNode != null ? labelNode.getName() : null;
		} else {
			continuationLocation =
					new Location(location.getContext(), node.getMark());
			label = null;
		}

		if (type == CONTINUED_INTERROGATION) {
			block.declare(continuationLocation)
			.alternative(continuationLocation)
			.loop(continuationLocation, label);
			return;
		}

		final List<? extends Statements<?>> alts = sentence.getAlternatives();

		if (alts.isEmpty()) {
			sentence.alternative(continuationLocation)
			.loop(continuationLocation, label);
			return;
		}

		for (Statements<?> alt : alts) {
			alt.loop(continuationLocation, label);
		}
	}

	private StInterpreter() {
	}

}

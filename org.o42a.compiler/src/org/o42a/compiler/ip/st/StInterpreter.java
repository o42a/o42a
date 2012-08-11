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
			block.getLogger().invalidExpression(node);
			return null;
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

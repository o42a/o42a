package org.o42a.parser.grammar.clause;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


final class ClauseIdParser implements Parser<ClauseIdNode> {

	public static final ClauseIdParser CLAUSE_ID = new ClauseIdParser();

	private ClauseIdParser() {
	}

	@Override
	public ClauseIdNode parse(ParserContext context) {
		switch (context.next()) {
		case '@':
			return context.parse(declarableAdapter());
		case '[':
			return context.parse(brackets());
		case '{':
			return context.parse(braces());
		case '+':
		case '-':
			return context.parse(unary());
		}

		final RefNode ref = context.parse(ref());

		if (ref == null) {
			return null;
		}

		final BinaryNode binary = context.parse(new BinaryClauseIdParser(ref));

		if (binary != null) {
			return binary;
		}

		final PhraseNode phrase = context.parse(phrase(ref));

		if (phrase != null) {
			return phrase;
		}

		if (ref instanceof ClauseIdNode) {
			return (ClauseIdNode) ref;
		}

		return null;
	}

}

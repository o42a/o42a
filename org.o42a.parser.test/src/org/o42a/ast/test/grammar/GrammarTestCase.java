/*
    Parser Tests
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
package org.o42a.ast.test.grammar;

import static org.junit.Assert.*;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.o42a.ast.Node;
import org.o42a.ast.expression.BlockNode;
import org.o42a.ast.expression.ClauseNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.sentence.AlternativeNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SerialNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserWorker;
import org.o42a.util.Source;


public class GrammarTestCase {

	protected ParserWorker worker;

	public static <T> T to(Class<? extends T> type, Object value) {
		assertNotNull("Value is null", value);
		assertTrue(
				value + " is of type " + value.getClass().getSimpleName()
				+ ", but " + type.getSimpleName() + " expected",
				type.isInstance(value));
		return type.cast(value);
	}

	public static void assertRange(long start, long end, Node node) {
		assertEquals("Wrong range start", start, node.getStart().offset());
		assertEquals("Wrong range end", end, node.getEnd().offset());
	}

	public static void assertName(String name, Node node) {

		final MemberRefNode ref = to(MemberRefNode.class, node);

		assertNull("Unexpected field owner", ref.getOwner());
		assertNull("Unexpected retention", ref.getDeclaredIn());
		assertNotNull("No name", ref.getName());
		assertEquals(name, ref.getName().getName());
	}

	public static <T extends StatementNode> T singleStatement(
			Class<? extends T> type,
			BlockNode<?> block) {

		final SentenceNode[] sentences = block.getContent();

		assertEquals(
				"Single sentence expected: " + block.getContent(),
				1,
				sentences.length);

		return singleStatement(type, sentences[0]);
	}

	public static <T extends StatementNode> T singleStatement(
			Class<? extends T> type,
			SentenceNode sentence) {
		return statement(type, sentence, 0, 1);
	}

	public static <T extends StatementNode> T statement(
			Class<? extends T> type,
			SentenceNode sentence,
			int index,
			int length) {

		final AlternativeNode[] disjunction = sentence.getDisjunction();

		assertEquals(
				"Single alternative expected: " + sentence,
				1,
				disjunction.length);


		final SerialNode[] conjunction = disjunction[0].getConjunction();

		assertEquals(
				"Wrong number of statements: " + sentence,
				length,
				conjunction.length);

		return to(type, conjunction[index].getStatement());
	}

	public static <T extends ClauseNode> T singleClause(
			Class<? extends T> type,
			PhraseNode phrase) {
		return clause(type, phrase, 0, 1);
	}

	public static <T extends ClauseNode> T clause(
			Class<? extends T> type,
			PhraseNode phrase,
			int index,
			int length) {

		final ClauseNode[] clauses = phrase.getClauses();

		assertEquals(
				"Wrong numer of phrase clauses: " + phrase,
				length,
				clauses.length);

		return to(type, clauses[index]);
	}

	public <T> T parse(Parser<T> parser, String text) {
		this.worker = new ParserWorker(new Src(text));
		this.worker.setLogger(DECLARATION_LOGGER);
		return this.worker.parse(parser);
	}

	protected class Src extends Source {

		private static final long serialVersionUID = -369778503973033190L;

		private final String text;

		public Src(String text) {
			this.text = text;
		}

		@Override
		public String getName() {
			return GrammarTestCase.this.getClass().getSimpleName();
		}

		@Override
		public Reader open() throws IOException {
			return new StringReader(this.text);
		}

	}

}

/*
    Parser Tests
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
package org.o42a.ast.test.grammar.atom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.o42a.parser.Grammar.comment;

import org.junit.Test;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class CommentTest extends GrammarTestCase {

	@Test
	public void singleLineComment() {

		final CommentNode comment = parse("//comment\n");

		assertEquals(0, comment.getStart().getOffset());
		assertEquals(9, comment.getEnd().getOffset());
		assertEquals(10, this.worker.position().offset());
		assertEquals("comment", comment.getText());
		assertFalse(comment.isMultiline());
	}

	@Test
	public void singleLineCommentEOF() {

		final CommentNode comment = parse("//comment");

		assertEquals(0, comment.getStart().getOffset());
		assertEquals(9, comment.getEnd().getOffset());
		assertEquals(9, this.worker.position().offset());
		assertEquals("comment", comment.getText());
		assertFalse(comment.isMultiline());
	}

	@Test
	public void multilineComment() {

		final CommentNode comment = parse("/*\ncomment\n*/");

		assertEquals(0, comment.getStart().getOffset());
		assertEquals(13, comment.getEnd().getOffset());
		assertEquals(13, this.worker.position().offset());
		assertEquals("\ncomment\n", comment.getText());
		assertTrue(comment.isMultiline());
	}

	@Test
	public void acceptWhitespaceAfterMultiLineComment() {
		parse("/**/  a");

		assertEquals(6, this.worker.position().offset());
	}

	@Test
	public void acceptWhitespaceAfterSingleLineComment() {
		parse("//\n  a");

		assertEquals(5, this.worker.position().offset());
	}

	private CommentNode parse(String text) {
		return parse(comment(true), text);
	}

}

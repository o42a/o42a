/*
    Parser Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.o42a.parser.Grammar.separator;

import org.junit.Test;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.atom.SeparatorNodes;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class SeparatorsTest extends GrammarTestCase {

	@Test
	public void sameLine() {

		final SeparatorNodes result = parse(false, "  \n");

		assertFalse(result.lineContinuation());
		assertThat(this.worker.position().line(), is(1));
		assertThat(this.worker.position().column(), is(3));
		assertThat(this.worker.position().offset(), is(2L));
	}

	@Test
	public void newLine() {

		final SeparatorNodes result = parse(true, "  \n");

		assertFalse(result.lineContinuation());
		assertThat(this.worker.position().line(), is(2));
		assertThat(this.worker.position().column(), is(1));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void lineContinuation() {

		final SeparatorNodes result = parse(false, "  \n _ ");

		assertTrue(result.lineContinuation());
		assertThat(this.worker.position().line(), is(2));
		assertThat(this.worker.position().column(), is(4));
		assertThat(this.worker.position().offset(), is(6L));
	}

	@Test
	public void singleLineComment() {

		final SeparatorNodes result =
				parse(false, "//comment1\n//comment2");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(this.worker.position().offset(), is(10L));
	}

	@Test
	public void inlineComment() {

		final SeparatorNodes result =
				parse(false, "~~comment1\n~~comment2");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(this.worker.position().offset(), is(10L));
	}

	@Test
	public void singleLineCommentsWithUnderscope() {

		final SeparatorNodes result =
				parse(false, "//comment1\n  _//comment2\n  ");

		assertTrue(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(14, 24, comments[1]);
		assertThat(this.worker.position().offset(), is(27L));
	}

	@Test
	public void inlineCommentsWithUnderscope() {

		final SeparatorNodes result =
				parse(false, "~~comment1\n  _~~comment2\n  ");

		assertTrue(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(14, 27, comments[1]);
		assertThat(this.worker.position().offset(), is(27L));
	}

	@Test
	public void singleLineCommentNL() {

		final SeparatorNodes result =
				parse(true, "//comment1\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(this.worker.position().offset(), is(13L));
	}

	@Test
	public void inineCommentNL() {

		final SeparatorNodes result =
				parse(true, "~~comment1\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 13, comments[0]);
		assertThat(this.worker.position().offset(), is(13L));
	}

	@Test
	public void singleLineCommentsNL() {

		final SeparatorNodes result =
				parse(true, "//comment1\n  //comment2\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 10, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(13, 23, comments[1]);
		assertThat(this.worker.position().offset(), is(26L));
	}

	@Test
	public void inineCommentsNL() {

		final SeparatorNodes result =
				parse(true, "~~comment1\n  ~~comment2\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertRange(0, 13, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(13, 26, comments[1]);
		assertThat(this.worker.position().offset(), is(26L));
	}

	@Test
	public void multiLineComments() {

		final SeparatorNodes result =
				parse(true, "/*\ncomment1\n*/ //comment2\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("\ncomment1\n"));
		assertRange(0, 14, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(15, 25, comments[1]);
		assertThat(this.worker.position().offset(), is(28L));
	}

	public void blockComments() {

		final SeparatorNodes result = parseLines(
				separator(true),
				"   ",
				"~~~~~~",
				"comment1",
				"~~~",
				" ",
				"~~~ comment2\n  ");

		assertFalse(result.lineContinuation());
		assertTrue(result.haveComments());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("\ncomment1\n"));
		assertRange(0, 14, comments[0]);
		assertThat(comments[1].getText(), is("comment2"));
		assertRange(15, 25, comments[1]);
		assertThat(this.worker.position().offset(), is(28L));
	}

	private SeparatorNodes parse(boolean allowNewLine, String text) {
		return parse(separator(allowNewLine), text);
	}

}

/*
    Parser Tests
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
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.CommentBound.INLINE_COMMENT;
import static org.o42a.parser.Grammar.comment;

import org.junit.Test;
import org.o42a.ast.atom.NewCommentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class InlineCommentTest extends GrammarTestCase {

	@Test
	public void inlineComment() {

		final NewCommentNode comment = parseInline("~~comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(9L));
		assertThat(this.worker.position().offset(), is(9L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void nl() {

		final NewCommentNode comment = parseNL("~~comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(10L));
		assertThat(this.worker.position().offset(), is(10L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void manyTildes() {

		final NewCommentNode comment = parseInline("~~~~ comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(12L));
		assertThat(this.worker.position().offset(), is(12L));
		assertThat(comment.getText(), is(" comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void manyTildesNL() {

		final NewCommentNode comment = parseNL("~~~~ comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(13L));
		assertThat(this.worker.position().offset(), is(13L));
		assertThat(comment.getText(), is(" comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void eof() {

		final NewCommentNode comment = parseInline("~~comment");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(9L));
		assertThat(this.worker.position().offset(), is(9L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void closed() {

		final NewCommentNode comment = parseInline("~~comment~~");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(11L));
		assertThat(this.worker.position().offset(), is(11L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing().getType(), is(INLINE_COMMENT));
	}

	@Test
	public void closedByManyTildes() {

		final NewCommentNode comment = parseInline("~~comment~~~~");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(13L));
		assertThat(this.worker.position().offset(), is(13L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing().getType(), is(INLINE_COMMENT));
	}

	@Test
	public void notClosed() {

		final NewCommentNode comment = parseInline("~~comment~\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(10L));
		assertThat(this.worker.position().offset(), is(10L));
		assertThat(comment.getText(), is("comment~"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void notClosedNL() {

		final NewCommentNode comment = parseNL("~~comment~\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(11L));
		assertThat(this.worker.position().offset(), is(11L));
		assertThat(comment.getText(), is("comment~"));
		assertThat(comment.getOpening().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void acceptTrailingWhitespace() {
		parseNL("~~\n  a");

		assertThat(this.worker.position().offset(), is(5L));
	}

	@Test
	public void dontAcceptTrailingWhitespace() {
		parseInline("~~\n  a");

		assertThat(this.worker.position().offset(), is(2L));
	}

	private NewCommentNode parseNL(String text) {
		return parse(text, true);
	}

	private NewCommentNode parseInline(String text) {
		return parse(text, false);
	}

	private NewCommentNode parse(String text, boolean allowNewLine) {
		return to(NewCommentNode.class, parse(comment(allowNewLine), text));
	}

}

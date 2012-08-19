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
import static org.o42a.ast.atom.CommentBound.BLOCK_COMMENT;
import static org.o42a.parser.Grammar.comment;

import org.junit.Test;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class BlockCommentTest extends GrammarTestCase {

	@Test
	public void blockComment() {

		final CommentNode comment = parse(
				"~~~",
				" comment",
				"  ~~~");

		assertThat(comment.getStart().getLine(), is(1));
		assertThat(comment.getEnd().getLine(), is(3));
		assertThat(this.worker.position().line(), is(4));
		assertThat(comment.getText().trim(), is("comment"));
		assertThat(comment.getOpening().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosing().getType(), is(BLOCK_COMMENT));
	}

	@Test
	public void eof() {

		final CommentNode comment = parse(
				"~~~",
				"comment");

		assertThat(comment.getStart().getLine(), is(1));
		assertThat(comment.getEnd().getLine(), is(3));
		assertThat(this.worker.position().line(), is(3));
		assertThat(comment.getText().trim(), is("comment"));
		assertThat(comment.getOpening().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosing(), nullValue());
	}

	@Test
	public void twoTildesInComment() {

		final CommentNode comment = parse(
				"~~~",
				"  ~~   ",
				"    ~~~");

		assertThat(comment.getStart().getLine(), is(1));
		assertThat(comment.getEnd().getLine(), is(3));
		assertThat(this.worker.position().line(), is(4));
		assertThat(comment.getText().trim(), is("~~"));
		assertThat(comment.getOpening().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosing().getType(), is(BLOCK_COMMENT));
	}

	@Test
	public void wordAfterTildesInsideComment() {

		final CommentNode comment = parse(
				"~~~",
				"~~~ comment",
				"~~~");

		assertThat(comment.getStart().getLine(), is(1));
		assertThat(comment.getEnd().getLine(), is(3));
		assertThat(this.worker.position().line(), is(4));
		assertThat(comment.getText().trim(), is("~~~ comment"));
		assertThat(comment.getOpening().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosing().getType(), is(BLOCK_COMMENT));
	}

	@Test
	public void wordBeforeTildesInsideComment() {

		final CommentNode comment = parse(
				"~~~",
				"comment ~~~",
				"~~~");

		assertThat(comment.getStart().getLine(), is(1));
		assertThat(comment.getEnd().getLine(), is(3));
		assertThat(this.worker.position().line(), is(4));
		assertThat(comment.getText().trim(), is("comment ~~~"));
		assertThat(comment.getOpening().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosing().getType(), is(BLOCK_COMMENT));
	}

	private CommentNode parse(String... text) {
		return to(CommentNode.class, parseLines(comment(true), text));
	}

}

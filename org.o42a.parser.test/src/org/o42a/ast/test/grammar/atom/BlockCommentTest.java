/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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
		assertThat(comment.getOpeningBound().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(BLOCK_COMMENT));
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
		assertThat(comment.getOpeningBound().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
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
		assertThat(comment.getOpeningBound().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(BLOCK_COMMENT));
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
		assertThat(comment.getOpeningBound().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(BLOCK_COMMENT));
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
		assertThat(comment.getOpeningBound().getType(), is(BLOCK_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(BLOCK_COMMENT));
	}

	private CommentNode parse(String... text) {
		return to(CommentNode.class, parseLines(comment(true), text));
	}

}

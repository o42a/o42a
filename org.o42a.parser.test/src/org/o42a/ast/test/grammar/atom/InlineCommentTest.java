/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.atom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.ast.atom.CommentBound.INLINE_COMMENT;
import static org.o42a.parser.Grammar.comment;

import org.junit.Test;
import org.o42a.ast.atom.CommentNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class InlineCommentTest extends GrammarTestCase {

	@Test
	public void inlineComment() {

		final CommentNode comment = parseInline("~~comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(9L));
		assertThat(this.worker.position().offset(), is(9L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void nl() {

		final CommentNode comment = parseNL("~~comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(9L));
		assertThat(this.worker.position().offset(), is(10L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void manyTildes() {

		final CommentNode comment = parseInline("~~~~ comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(12L));
		assertThat(this.worker.position().offset(), is(12L));
		assertThat(comment.getText(), is(" comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void manyTildesNL() {

		final CommentNode comment = parseNL("~~~~ comment\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(12L));
		assertThat(this.worker.position().offset(), is(13L));
		assertThat(comment.getText(), is(" comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void eof() {

		final CommentNode comment = parseInline("~~comment");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(9L));
		assertThat(this.worker.position().offset(), is(9L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void closed() {

		final CommentNode comment = parseInline("~~comment~~");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(11L));
		assertThat(this.worker.position().offset(), is(11L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(INLINE_COMMENT));
	}

	@Test
	public void closedByManyTildes() {

		final CommentNode comment = parseInline("~~comment~~~~");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(13L));
		assertThat(this.worker.position().offset(), is(13L));
		assertThat(comment.getText(), is("comment"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound().getType(), is(INLINE_COMMENT));
	}

	@Test
	public void notClosed() {

		final CommentNode comment = parseInline("~~comment~\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(10L));
		assertThat(this.worker.position().offset(), is(10L));
		assertThat(comment.getText(), is("comment~"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
	}

	@Test
	public void notClosedNL() {

		final CommentNode comment = parseNL("~~comment~\n");

		assertThat(comment.getStart().getOffset(), is(0L));
		assertThat(comment.getEnd().getOffset(), is(10L));
		assertThat(this.worker.position().offset(), is(11L));
		assertThat(comment.getText(), is("comment~"));
		assertThat(comment.getOpeningBound().getType(), is(INLINE_COMMENT));
		assertThat(comment.getClosingBound(), nullValue());
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

	private CommentNode parseNL(String text) {
		return parse(text, true);
	}

	private CommentNode parseInline(String text) {
		return parse(text, false);
	}

	private CommentNode parse(String text, boolean allowNewLine) {
		return to(CommentNode.class, parse(comment(allowNewLine), text));
	}

}

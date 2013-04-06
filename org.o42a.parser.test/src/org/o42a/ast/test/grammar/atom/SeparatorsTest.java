/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
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

		assertTrue(result.isWhitespace());
		assertFalse(result.isLineContinuation());
		assertThat(this.worker.position().line(), is(1));
		assertThat(this.worker.position().column(), is(3));
		assertThat(this.worker.position().offset(), is(2L));
	}

	@Test
	public void newLine() {

		final SeparatorNodes result = parse(true, "  \n");

		assertTrue(result.isWhitespace());
		assertFalse(result.isLineContinuation());
		assertThat(this.worker.position().line(), is(2));
		assertThat(this.worker.position().column(), is(1));
		assertThat(this.worker.position().offset(), is(3L));
	}

	@Test
	public void lineContinuation() {

		final SeparatorNodes result = parse(false, "  \n _ ");

		assertTrue(result.haveUnderscores());
		assertFalse(result.haveComments());
		assertTrue(result.isLineContinuation());
		assertThat(result.getLineContinuation().getOffset(), is(4L));

		assertThat(this.worker.position().line(), is(2));
		assertThat(this.worker.position().column(), is(4));
		assertThat(this.worker.position().offset(), is(6L));
	}

	@Test
	public void inlineComment() {

		final SeparatorNodes result =
				parse(false, "~~comment1\n~~comment2");

		assertFalse(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertThat(comments[0], hasRange(0, 10));
		assertThat(this.worker.position().offset(), is(10L));
	}

	@Test
	public void inlineCommentsWithUnderscope() {

		final SeparatorNodes result =
				parse(true, "~~comment1\n  _~~comment2\n  ");

		assertTrue(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertThat(comments[0], hasRange(0, 10));
		assertThat(comments[1].getText(), is("comment2"));
		assertThat(comments[1], hasRange(14, 24));
		assertThat(this.worker.position().offset(), is(27L));
	}

	@Test
	public void lineContinuationWithComment() {

		final SeparatorNodes result =
				parse(false, "  __ ~~comment~~");

		assertTrue(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertTrue(result.isLineContinuation());
		assertThat(result.getLineContinuation().getColumn(), is(3));

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment"));
		assertThat(comments[0], hasRange(5, 16));
		assertThat(this.worker.position().offset(), is(16L));
	}

	@Test
	public void notLineContinuation() {

		final SeparatorNodes result =
				parse(true, "_ ~~comment\n  ");

		assertTrue(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment"));
		assertThat(comments[0], hasRange(2, 11));
		assertThat(this.worker.position().offset(), is(14L));
	}

	@Test
	public void inineCommentNL() {

		final SeparatorNodes result =
				parse(true, "~~comment1\n  ");

		assertFalse(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(1));
		assertThat(comments[0].getText(), is("comment1"));
		assertThat(comments[0], hasRange(0, 10));
		assertThat(this.worker.position().offset(), is(13L));
	}

	@Test
	public void inineCommentsNL() {

		final SeparatorNodes result =
				parse(true, "~~comment1\n  ~~comment2\n  ");

		assertFalse(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1"));
		assertThat(comments[0], hasRange(0, 10));
		assertThat(comments[1].getText(), is("comment2"));
		assertThat(comments[1], hasRange(13, 23));
		assertThat(this.worker.position().offset(), is(26L));
	}

	@Test
	public void blockComments() {

		final SeparatorNodes result = parseLines(
				separator(true),
				"   ",
				"~~~~~~",
				"comment1",
				"~~~",
				" ",
				"~~~ comment2\n  ");

		assertFalse(result.haveUnderscores());
		assertTrue(result.haveComments());
		assertFalse(result.isLineContinuation());

		final CommentNode[] comments = result.getComments();

		assertThat(comments.length, is(2));
		assertThat(comments[0].getText(), is("comment1\n"));
		assertThat(comments[0], hasRange(4, 23));
		assertThat(comments[1].getText(), is(" comment2"));
		assertThat(comments[1], hasRange(26, 38));
		assertThat(this.worker.position().offset(), is(42L));
	}

	private SeparatorNodes parse(boolean allowNewLine, String text) {
		return parse(separator(allowNewLine), text);
	}

}

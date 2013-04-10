/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.expression;

import org.junit.Test;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.BinaryOperator;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ImplicitSuffixTest extends GrammarTestCase {

	@Test
	public void macroReference() {

		final BinaryNode binary = parse("a ##b");

		assertThat(binary.getOperator(), is(BinaryOperator.SUFFIX));
		assertThat(binary.getSign(), hasRange(2, 4));
		assertThat(binary.getLeftOperand(), isName("a"));

		final MemberRefNode right =
				to(MemberRefNode.class, binary.getRightOperand());

		assertThat(right, hasName("b"));
		assertThat(
				to(ScopeRefNode.class, right.getOwner()).getType(),
				is(ScopeType.MACROS));
	}

	@Test
	public void macroExpression() {

		final BinaryNode binary = parse("a ##foo [bar]");

		assertThat(binary.getOperator(), is(BinaryOperator.SUFFIX));
		assertThat(binary.getSign(), hasRange(2, 4));
		assertThat(binary.getLeftOperand(), isName("a"));

		final PhraseNode phrase =
				to(PhraseNode.class, binary.getRightOperand());
		final MemberRefNode prefix =
				to(MemberRefNode.class, phrase.getPrefix());

		assertThat(prefix, hasName("foo"));
		assertThat(
				to(ScopeRefNode.class, prefix.getOwner()).getType(),
				is(ScopeType.MACROS));
	}

	private BinaryNode parse(String text) {
		return to(BinaryNode.class, parse(expression(), text));
	}

}

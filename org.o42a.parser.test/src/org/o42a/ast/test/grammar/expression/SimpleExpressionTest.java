/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.TypeArgumentsNode;


public class SimpleExpressionTest extends GrammarTestCase {

	@Test
	public void memberRef() {
		assertThat(parse("foo: bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo\\: bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo (): bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo): bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo)->bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo ()->bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo)>>bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo ()>>bar"), instanceOf(MemberRefNode.class));
	}

	@Test
	public void macroRef() {
		assertThat(parse("foo #bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo\\ #bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo () #bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo) #bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo)->#bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo ()->#bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("(foo)>>#bar"), instanceOf(MemberRefNode.class));
		assertThat(parse("foo ()>>#bar"), instanceOf(MemberRefNode.class));
	}

	@Test
	public void adapterRef() {
		assertThat(parse("foo @@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("foo\\ @@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("foo () @@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("(foo) @@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("foo ()->@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("(foo)->@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("foo ()>>@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("(foo)>>@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("*@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse(":@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("::@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("$@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("/@@bar"), instanceOf(AdapterRefNode.class));
		assertThat(parse("//@@bar"), instanceOf(AdapterRefNode.class));
	}

	@Test
	public void deref() {
		assertThat(parse("(foo)->"), instanceOf(DerefNode.class));
		assertThat(parse("foo\\->"), instanceOf(DerefNode.class));
		assertThat(parse("foo->"), instanceOf(DerefNode.class));
		assertThat(parse("foo ()->"), instanceOf(DerefNode.class));
		assertThat(parse("\"\"->"), instanceOf(DerefNode.class));
		assertThat(parse("123 456->"), instanceOf(DerefNode.class));
		assertThat(parse("foo _bar->"), instanceOf(DerefNode.class));
		assertThat(parse("foo::->"), instanceOf(DerefNode.class));
	}

	@Test
	public void eagerRef() {
		assertThat(parse("(foo)>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("foo\\>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("foo>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("foo ()>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("\"\">>"), instanceOf(EagerRefNode.class));
		assertThat(parse("123 456>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("foo _bar>>"), instanceOf(EagerRefNode.class));
		assertThat(parse("foo::>>"), instanceOf(EagerRefNode.class));
	}

	@Test
	public void scopeRef() {
		assertThat(parse("*"), instanceOf(ScopeRefNode.class));
		assertThat(parse(":"), instanceOf(ScopeRefNode.class));
		assertThat(parse("::"), instanceOf(ScopeRefNode.class));
		assertThat(parse("#"), instanceOf(ScopeRefNode.class));
		assertThat(parse("##"), instanceOf(ScopeRefNode.class));
		assertThat(parse("$"), instanceOf(ScopeRefNode.class));
		assertThat(parse("$$"), instanceOf(ScopeRefNode.class));
	}

	@Test
	public void parentRef() {
		assertThat(parse("foo::"), instanceOf(ParentRefNode.class));
	}

	@Test
	public void unaryExpression() {
		assertThat(parse("`foo"), instanceOf(UnaryNode.class));
		assertThat(parse("``foo"), instanceOf(UnaryNode.class));
		assertThat(parse("+foo"), instanceOf(UnaryNode.class));
		assertThat(parse("-foo"), instanceOf(UnaryNode.class));
		assertThat(parse("−foo"), instanceOf(UnaryNode.class));
		assertThat(parse("++foo"), instanceOf(UnaryNode.class));
		assertThat(parse("--foo"), instanceOf(UnaryNode.class));
		assertThat(parse("¬foo"), instanceOf(UnaryNode.class));
		assertThat(parse("\\foo"), instanceOf(UnaryNode.class));
		assertThat(parse("\\\\foo"), instanceOf(UnaryNode.class));
		assertThat(parse("+foo\\ bar"), instanceOf(UnaryNode.class));
	}

	@Test
	public void parentheses() {
		assertThat(parse("(foo)"), instanceOf(ParenthesesNode.class));
		assertThat(parse("(foo\\)"), instanceOf(ParenthesesNode.class));
	}

	@Test
	public void call() {
		assertThat(parse("foo ()"), instanceOf(PhraseNode.class));
	}

	@Test
	public void group() {
		assertThat(parse("foo\\"), instanceOf(GroupNode.class));
		assertThat(parse("foo ()\\"), instanceOf(GroupNode.class));
		assertThat(parse("foo _bar ()\\"), instanceOf(GroupNode.class));
		assertThat(parse("foo\\ bar\\"), instanceOf(GroupNode.class));
		assertThat(parse("5\\"), instanceOf(GroupNode.class));
	}

	@Test
	public void qualifiedString() {
		assertThat(parse("foo 'bar'"), instanceOf(PhraseNode.class));
		assertThat(parse("foo \"bar\""), instanceOf(PhraseNode.class));
	}

	@Test
	public void simplePhrase() {
		assertThat(parse("foo {bar}"), instanceOf(PhraseNode.class));
		assertThat(parse("foo [bar]"), instanceOf(PhraseNode.class));
	}

	@Test
	public void phrase() {
		assertThat(parse("foo _bar"), instanceOf(PhraseNode.class));
		assertThat(parse("foo\\ bar"), instanceOf(PhraseNode.class));
		assertThat(parse("(foo) bar"), instanceOf(PhraseNode.class));
		assertThat(parse("foo\\ bar"), instanceOf(PhraseNode.class));
	}

	@Test
	public void text() {
		assertThat(parse("'foo'"), instanceOf(TextNode.class));
		assertThat(parse("\"foo\""), instanceOf(TextNode.class));
		assertThat(parse("'foo' 'bar'"), instanceOf(TextNode.class));
		assertThat(parse("\"foo\" 'bar'"), instanceOf(TextNode.class));
		to(TextNode.class, parse(
				"'''",
				"foo",
				"''''",
				"'bar'"));
	}

	@Test
	public void array() {
		assertThat(parse("[a, b, c]"), instanceOf(BracketsNode.class));
	}

	@Test
	public void decimalInteger() {
		assertThat(parse("123"), instanceOf(NumberNode.class));
		assertThat(parse("+ 123"), instanceOf(NumberNode.class));
		assertThat(parse("- 123"), instanceOf(NumberNode.class));
		assertThat(parse("− 123"), instanceOf(NumberNode.class));
	}

	@Test
	public void floatNumber() {
		assertThat(parse("123.456"), instanceOf(NumberNode.class));
		assertThat(parse("+ 123e123"), instanceOf(NumberNode.class));
		assertThat(parse("- 123.4e-56"), instanceOf(NumberNode.class));
		assertThat(parse("− 123.4e-56"), instanceOf(NumberNode.class));
	}

	@Test
	public void hexNumber() {
		assertThat(parse("0xf123"), instanceOf(NumberNode.class));
		assertThat(parse("+0x123"), instanceOf(NumberNode.class));
		assertThat(parse("-0x123"), instanceOf(NumberNode.class));
		assertThat(parse("−0x123"), instanceOf(NumberNode.class));
	}

	@Test
	public void binaryNumber() {
		assertThat(parse("0b1001"), instanceOf(NumberNode.class));
		assertThat(parse("+0x101"), instanceOf(NumberNode.class));
		assertThat(parse("-0x001"), instanceOf(NumberNode.class));
		assertThat(parse("−0x001"), instanceOf(NumberNode.class));
	}

	@Test
	public void typeArgument() {
		assertThat(parse("foo` bar"), instanceOf(TypeArgumentsNode.class));
		assertThat(
				parse("foo# bar` baz"),
				instanceOf(TypeArgumentsNode.class));
		assertThat(parse("#foo` bar"), instanceOf(TypeArgumentsNode.class));
		assertThat(parse("##foo` bar"), instanceOf(TypeArgumentsNode.class));
		assertThat(
				parse("(##foo [bar])` baz"),
				instanceOf(TypeArgumentsNode.class));
		assertThat(
				parse("(foo, bar)` baz"),
				instanceOf(TypeArgumentsNode.class));
	}

	private Node parse(String... text) {
		return parseLines(simpleExpression(), text);
	}

}

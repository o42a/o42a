/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.expression;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.simpleExpression;

import org.junit.Test;
import org.o42a.ast.Node;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentNode;


public class SimpleExpressionTest extends GrammarTestCase {

	@Test
	public void memberRef() {
		assertThat(parse("foo: bar"), is(MemberRefNode.class));
		assertThat(parse("foo\\: bar"), is(MemberRefNode.class));
		assertThat(parse("foo (): bar"), is(MemberRefNode.class));
		assertThat(parse("(foo): bar"), is(MemberRefNode.class));
		assertThat(parse("(foo) -> bar"), is(MemberRefNode.class));
		assertThat(parse("foo () -> bar"), is(MemberRefNode.class));
	}

	@Test
	public void macroRef() {
		assertThat(parse("foo #bar"), is(MemberRefNode.class));
		assertThat(parse("foo\\ #bar"), is(MemberRefNode.class));
		assertThat(parse("foo () #bar"), is(MemberRefNode.class));
		assertThat(parse("(foo) #bar"), is(MemberRefNode.class));
		assertThat(parse("(foo) -> #bar"), is(MemberRefNode.class));
		assertThat(parse("foo () -> #bar"), is(MemberRefNode.class));
	}

	@Test
	public void adapterRef() {
		assertThat(parse("foo @@bar"), is(AdapterRefNode.class));
		assertThat(parse("foo\\ @@bar"), is(AdapterRefNode.class));
		assertThat(parse("foo () @@bar"), is(AdapterRefNode.class));
		assertThat(parse("(foo) @@bar"), is(AdapterRefNode.class));
		assertThat(parse("foo () -> @@bar"), is(AdapterRefNode.class));
		assertThat(parse("(foo) -> @@bar"), is(AdapterRefNode.class));
		assertThat(parse("*@@bar"), is(AdapterRefNode.class));
		assertThat(parse(":@@bar"), is(AdapterRefNode.class));
		assertThat(parse("::@@bar"), is(AdapterRefNode.class));
		assertThat(parse("$@@bar"), is(AdapterRefNode.class));
		assertThat(parse("/@@bar"), is(AdapterRefNode.class));
		assertThat(parse("//@@bar"), is(AdapterRefNode.class));
	}

	@Test
	public void deref() {
		assertThat(parse("(foo)->"), is(DerefNode.class));
		assertThat(parse("foo\\->"), is(DerefNode.class));
		assertThat(parse("foo->"), is(DerefNode.class));
		assertThat(parse("foo ()->"), is(DerefNode.class));
		assertThat(parse("\"\"->"), is(DerefNode.class));
		assertThat(parse("123 456->"), is(DerefNode.class));
		assertThat(parse("foo_ bar->"), is(DerefNode.class));
		assertThat(parse("foo::->"), is(DerefNode.class));
	}

	@Test
	public void scopeRef() {
		assertThat(parse("*"), is(ScopeRefNode.class));
		assertThat(parse(":"), is(ScopeRefNode.class));
		assertThat(parse("::"), is(ScopeRefNode.class));
		assertThat(parse("##"), is(ScopeRefNode.class));
		assertThat(parse("$"), is(ScopeRefNode.class));
		assertThat(parse("$$"), is(ScopeRefNode.class));
	}

	@Test
	public void parentRef() {
		assertThat(parse("foo::"), is(ParentRefNode.class));
	}

	@Test
	public void unaryExpression() {
		assertThat(parse("`foo"), is(UnaryNode.class));
		assertThat(parse("``foo"), is(UnaryNode.class));
		assertThat(parse("+foo"), is(UnaryNode.class));
		assertThat(parse("-foo"), is(UnaryNode.class));
		assertThat(parse("−foo"), is(UnaryNode.class));
		assertThat(parse("++foo"), is(UnaryNode.class));
		assertThat(parse("--foo"), is(UnaryNode.class));
		assertThat(parse("¬foo"), is(UnaryNode.class));
		assertThat(parse("\\foo"), is(UnaryNode.class));
		assertThat(parse("\\\\foo"), is(UnaryNode.class));
		assertThat(parse("+foo\\ bar"), is(UnaryNode.class));
	}

	@Test
	public void macroExpansion() {
		assertThat(parse("#foo: bar"), is(MacroExpansionNode.class));
		assertThat(parse("# ::"), is(MacroExpansionNode.class));
		assertThat(parse("#(abc)"), is(MacroExpansionNode.class));
		assertThat(parse("# abc ()"), is(MacroExpansionNode.class));
		assertThat(parse("# #foo"), is(MacroExpansionNode.class));
	}

	@Test
	public void parentheses() {
		assertThat(parse("(foo)"), is(ParenthesesNode.class));
		assertThat(parse("(foo\\)"), is(ParenthesesNode.class));
	}

	@Test
	public void ascendants() {
		assertThat(parse("&foo"), is(AscendantsNode.class));
		assertThat(parse("foo & bar"), is(AscendantsNode.class));
		assertThat(parse("&foo & bar"), is(AscendantsNode.class));
	}

	@Test
	public void call() {
		assertThat(parse("foo ()"), is(PhraseNode.class));
		assertThat(parse("&foo ()"), is(PhraseNode.class));
		assertThat(parse("foo & bar ()"), is(PhraseNode.class));
		assertThat(parse("&foo & bar ()"), is(PhraseNode.class));
		assertThat(parse("* ()"), is(PhraseNode.class));
	}

	@Test
	public void group() {
		assertThat(parse("foo\\"), is(GroupNode.class));
		assertThat(parse("foo ()\\"), is(GroupNode.class));
		assertThat(parse("foo _bar ()\\"), is(GroupNode.class));
		assertThat(parse("foo\\ bar\\"), is(GroupNode.class));
		assertThat(parse("5\\"), is(GroupNode.class));
	}

	@Test
	public void qualifiedString() {
		assertThat(parse("foo 'bar'"), is(PhraseNode.class));
		assertThat(parse("foo \"bar\""), is(PhraseNode.class));
	}

	@Test
	public void simplePhrase() {
		assertThat(parse("foo {bar}"), is(PhraseNode.class));
		assertThat(parse("foo [bar]"), is(PhraseNode.class));
	}

	@Test
	public void phrase() {
		assertThat(parse("foo _bar"), is(PhraseNode.class));
		assertThat(parse("foo\\ bar"), is(PhraseNode.class));
		assertThat(parse("(foo) bar"), is(PhraseNode.class));
		assertThat(parse("foo\\ bar"), is(PhraseNode.class));
	}

	@Test
	public void text() {
		assertThat(parse("'foo'"), is(TextNode.class));
		assertThat(parse("\"foo\""), is(TextNode.class));
		assertThat(parse("'foo' 'bar'"), is(TextNode.class));
		assertThat(parse("\"foo\" 'bar'"), is(TextNode.class));
		to(TextNode.class, parse(
				"'''",
				"foo",
				"''''",
				"'bar'"));
	}

	@Test
	public void array() {
		assertThat(parse("[a, b, c]"), is(BracketsNode.class));
	}

	@Test
	public void decimalInteger() {
		assertThat(parse("123"), is(NumberNode.class));
		assertThat(parse("+ 123"), is(NumberNode.class));
		assertThat(parse("- 123"), is(NumberNode.class));
		assertThat(parse("− 123"), is(NumberNode.class));
	}

	@Test
	public void floatNumber() {
		assertThat(parse("123.456"), is(NumberNode.class));
		assertThat(parse("+ 123e123"), is(NumberNode.class));
		assertThat(parse("- 123.4e-56"), is(NumberNode.class));
		assertThat(parse("− 123.4e-56"), is(NumberNode.class));
	}

	@Test
	public void hexNumber() {
		assertThat(parse("0xf123"), is(NumberNode.class));
		assertThat(parse("+0x123"), is(NumberNode.class));
		assertThat(parse("-0x123"), is(NumberNode.class));
		assertThat(parse("−0x123"), is(NumberNode.class));
	}

	@Test
	public void binaryNumber() {
		assertThat(parse("0b1001"), is(NumberNode.class));
		assertThat(parse("+0x101"), is(NumberNode.class));
		assertThat(parse("-0x001"), is(NumberNode.class));
		assertThat(parse("−0x001"), is(NumberNode.class));
	}

	@Test
	public void typeArgument() {
		assertThat(parse("foo` bar"), is(TypeArgumentNode.class));
		assertThat(parse("foo# bar` baz"), is(TypeArgumentNode.class));
		assertThat(parse("(#foo)` bar"), is(TypeArgumentNode.class));
		assertThat(parse("(##foo)` bar"), is(TypeArgumentNode.class));
		assertThat(parse("(##foo [bar])` baz"), is(TypeArgumentNode.class));
		assertThat(parse("(foo, bar)` baz"), is(TypeArgumentNode.class));
	}

	private Node parse(String... text) {
		return parseLines(simpleExpression(), text);
	}

}

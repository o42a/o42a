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
package org.o42a.ast.test.grammar.expression;

import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.Node;
import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.ref.*;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class SimpleExpressionTest extends GrammarTestCase {

	@Test
	public void fieldRef() {
		to(MemberRefNode.class, parse("foo:bar"));
		to(MemberRefNode.class, parse("foo():bar"));
		to(MemberRefNode.class, parse("(foo):bar"));
	}

	@Test
	public void adapterRef() {
		to(AdapterRefNode.class, parse("foo@@bar"));
		to(AdapterRefNode.class, parse("foo()@@bar"));
		to(AdapterRefNode.class, parse("(foo)@@bar"));
		to(AdapterRefNode.class, parse("*@@bar"));
		to(AdapterRefNode.class, parse(":@@bar"));
		to(AdapterRefNode.class, parse("::@@bar"));
		to(AdapterRefNode.class, parse("$@@bar"));
		to(AdapterRefNode.class, parse("$$@@bar"));
		to(AdapterRefNode.class, parse("^@@bar"));
	}

	@Test
	public void scopeRef() {
		to(ScopeRefNode.class, parse("*"));
		to(ScopeRefNode.class, parse(":"));
		to(ScopeRefNode.class, parse("::"));
		to(ScopeRefNode.class, parse("$"));
		to(ScopeRefNode.class, parse("$$"));
	}

	@Test
	public void parentRef() {
		to(ParentRefNode.class, parse("foo::"));
	}

	@Test
	public void intrinsicRef() {
		to(IntrinsicRefNode.class, parse("$foo$"));
	}

	@Test
	public void ascendantRef() {
		to(AscendantRefNode.class, parse("^"));
		to(AscendantRefNode.class, parse(":^"));
		to(AscendantRefNode.class, parse("::^"));
		to(AscendantRefNode.class, parse("$^"));
		to(AscendantRefNode.class, parse("$$^"));
		to(AscendantRefNode.class, parse("$foo$^"));
		to(AscendantRefNode.class, parse("foo::^"));

		to(AscendantRefNode.class, parse("^foo^"));
		to(AscendantRefNode.class, parse(":^foo^"));
		to(AscendantRefNode.class, parse("::^foo^"));
		to(AscendantRefNode.class, parse("$^foo^"));
		to(AscendantRefNode.class, parse("$$^foo^"));
		to(AscendantRefNode.class, parse("$foo$^foo^"));
		to(AscendantRefNode.class, parse("foo::^bar^"));
	}

	@Test
	public void unaryExpression() {
		to(UnaryNode.class, parse("+foo"));
		to(UnaryNode.class, parse("-foo"));
		to(UnaryNode.class, parse("\u2212 foo"));
		to(UnaryNode.class, parse("++foo"));
		to(UnaryNode.class, parse("--foo"));
		to(UnaryNode.class, parse("+-foo"));
		to(UnaryNode.class, parse("-+foo"));
	}

	@Test
	public void parentheses() {
		to(ParenthesesNode.class, parse("(foo)"));
	}

	@Test
	public void ascendants() {
		to(AscendantsNode.class, parse("&foo"));
		to(AscendantsNode.class, parse("foo & bar"));
		to(AscendantsNode.class, parse("&foo & bar"));
	}

	@Test
	public void call() {
		to(PhraseNode.class, parse("foo()"));
		to(PhraseNode.class, parse("&foo()"));
		to(PhraseNode.class, parse("foo & bar()"));
		to(PhraseNode.class, parse("&foo & bar()"));
		to(PhraseNode.class, parse("*()"));
	}

	@Test
	public void qualifiedString() {
		to(PhraseNode.class, parse("foo 'bar'"));
		to(PhraseNode.class, parse("foo \"bar\""));
	}

	@Test
	public void simplePhrase() {
		to(PhraseNode.class, parse("foo{bar}"));
		to(PhraseNode.class, parse("foo[bar]"));
	}

	@Test
	public void text() {
		to(TextNode.class, parse("'foo'"));
		to(TextNode.class, parse("\"foo\""));
		to(TextNode.class, parse("'foo' 'bar'"));
		to(TextNode.class, parse("\"foo\" 'bar'"));
		to(TextNode.class, parse("\\'foo'\\ 'bar'"));
	}

	@Test
	public void array() {
		to(BracketsNode.class, parse("[`a, b, c]"));
		to(BracketsNode.class, parse("[``a, b, c]"));
		to(BracketsNode.class, parse("[(`foo) a, b, c]"));
		to(BracketsNode.class, parse("[(``foo) a, b, c]"));
	}

	@Test
	public void decimalLiteral() {
		to(DecimalNode.class, parse("123"));
	}

	private Node parse(String text) {
		return parse(DECLARATIVE.simpleExpression(), text);
	}

}

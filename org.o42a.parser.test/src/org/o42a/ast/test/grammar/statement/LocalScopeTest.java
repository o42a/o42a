/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.statement;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.statement.LocalNode;
import org.o42a.ast.statement.LocalScopeNode;
import org.o42a.ast.statement.NamedBlockNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class LocalScopeTest extends GrammarTestCase {

	@Test
	public void parentheses() {

		final LocalScopeNode scope = parse("Expression $ ()");

		checkExpressionIs(scope, "expression");
		checkAnonymous(scope);
		checkNoColon(scope);
		assertThat(scope.getContent(), instanceOf(ParenthesesNode.class));
	}

	@Test
	public void parenthesesWithColon() {

		final LocalScopeNode scope = parse("Expression $: ()");

		checkExpressionIs(scope, "expression");
		checkAnonymous(scope);
		checkColon(scope);
		assertThat(scope.getContent(), instanceOf(ParenthesesNode.class));
	}

	@Test
	public void braces() {

		final LocalScopeNode scope = parse("Expression $ {}");

		checkExpressionIs(scope, "expression");
		checkAnonymous(scope);
		checkNoColon(scope);
		assertThat(scope.getContent(), instanceOf(BracesNode.class));
	}

	@Test
	public void bracesWithColon() {

		final LocalScopeNode scope = parse("Expression $: {}");

		checkExpressionIs(scope, "expression");
		checkAnonymous(scope);
		checkColon(scope);
		assertThat(scope.getContent(), instanceOf(BracesNode.class));
	}

	@Test
	public void namedBlock() {

		final LocalScopeNode scope = parse(
				"Expression $:",
				"block: {}");

		checkExpressionIs(scope, "expression");
		checkAnonymous(scope);
		checkColon(scope);

		final NamedBlockNode block =
				to(NamedBlockNode.class, scope.getContent());

		assertThat(canonicalName(block.getName()), is("block"));
	}

	@Test
	public void namedBlockAndNamedLocal() {

		final LocalScopeNode scope = parse(
				"Expression $ local:",
				"block: {}");

		checkExpressionIs(scope, "expression");
		checkLocalNameIs(scope, "local");
		checkColon(scope);

		final NamedBlockNode block =
				to(NamedBlockNode.class, scope.getContent());

		assertThat(canonicalName(block.getName()), is("block"));
	}

	@Test
	public void nested() {

		final LocalScopeNode scope = parse(
				"Expression1 $ local 1:",
				"Expression2 $ local 2:",
				"Statement");

		checkExpressionIs(scope, "expression1");
		checkLocalNameIs(scope, "local1");
		checkColon(scope);

		final LocalScopeNode nested =
				to(LocalScopeNode.class, scope.getContent());

		checkExpressionIs(nested, "expression2");
		checkLocalNameIs(nested, "local2");
		checkContentIs(nested, "statement");
		checkColon(nested);
	}

	@Test
	public void localLink() {

		final LocalScopeNode scope = parse("`Expression $ local: statement");
		final UnaryNode expression =
				to(UnaryNode.class, scope.getLocal().getExpression());

		assertThat(expression.getOperator(), is(UnaryOperator.LINK));
		assertThat(expression.getOperand(), isName("expression"));
		checkLocalNameIs(scope, "local");
		checkContentIs(scope, "statement");
	}

	@Test
	public void localVariable() {

		final LocalScopeNode scope = parse("``Expression $ local: statement");
		final UnaryNode expression =
				to(UnaryNode.class, scope.getLocal().getExpression());

		assertThat(expression.getOperator(), is(UnaryOperator.VARIABLE));
		assertThat(expression.getOperand(), isName("expression"));
		checkLocalNameIs(scope, "local");
		checkContentIs(scope, "statement");
	}

	private LocalScopeNode parse(String... text) {
		return to(
				LocalScopeNode.class,
				parseLines(
						DECLARATIVE.statement(),
						text));
	}

	private static void checkExpressionIs(
			LocalScopeNode scope,
			String expression) {
		assertThat(scope.getLocal().getExpression(), isName(expression));
	}

	private static void checkAnonymous(LocalScopeNode scope) {

		final LocalNode local = scope.getLocal();

		assertThat(
				local.getSeparator().getType(),
				is(LocalNode.Separator.DOLLAR_SIGN));
		assertThat(local.getName(), nullValue());
	}

	private static void checkLocalNameIs(LocalScopeNode scope, String name) {

		final LocalNode local = scope.getLocal();

		assertThat(
				local.getSeparator().getType(),
				is(LocalNode.Separator.DOLLAR_SIGN));
		assertThat(canonicalName(local.getName()), is(name));
	}

	private static void checkNoColon(LocalScopeNode scope) {
		assertThat(scope.getSeparator(), nullValue());
	}

	private static void checkColon(LocalScopeNode scope) {
		assertThat(
				scope.getSeparator().getType(),
				is(LocalScopeNode.Separator.COLON));
	}

	private static void checkContentIs(LocalScopeNode scope, String content) {
		assertThat(scope.getContent(), isName(content));
	}

}

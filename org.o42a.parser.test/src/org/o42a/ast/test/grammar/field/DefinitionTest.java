/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.declarator;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.expression.*;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.StaticRefNode;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.StringSource;


public class DefinitionTest extends GrammarTestCase {

	@Test
	public void link() {

		final DeclaratorNode result = parse("foo := `bar");
		final UnaryNode definition =
				to(UnaryNode.class, result.getDefinition());

		assertThat(result.getTarget(), is(DeclarationTarget.VALUE));
		assertThat(definition.getOperator(), is(UnaryOperator.LINK));
		assertThat(definition.getOperand(), isName("bar"));
	}

	@Test
	public void staticLink() {

		final DeclaratorNode result = parse("foo := `&bar");
		final UnaryNode definition =
				to(UnaryNode.class, result.getDefinition());

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertThat(definition.getOperator(), is(UnaryOperator.LINK));

		final StaticRefNode ascendants =
				to(StaticRefNode.class, definition.getOperand());

		assertThat(ascendants.getRef(), isName("bar"));
	}

	@Test
	public void variable() {

		final DeclaratorNode result = parse("foo := ``bar");
		final UnaryNode definition =
				to(UnaryNode.class, result.getDefinition());

		assertThat(result.getTarget(), is(DeclarationTarget.VALUE));
		assertThat(definition.getOperator(), is(UnaryOperator.VARIABLE));
		assertThat(definition.getOperand(), isName("bar"));
	}

	@Test
	public void initializer() {

		final DeclaratorNode result = parse("foo := bar = baz");

		assertThat(result.getTarget(), is(DeclarationTarget.VALUE));

		final PhraseNode definition =
				to(PhraseNode.class, result.getDefinition());

		assertThat(definition.getPrefix(), isName("bar"));
		assertThat(definition.getParts().length, is(1));

		final BracketsNode initializer =
				to(BracketsNode.class, definition.getParts()[0]);

		assertThat(initializer.getOpening(), nullValue());
		assertThat(initializer.getArguments().length, is(1));
		assertThat(initializer.getClosing(), nullValue());

		final ArgumentNode arg = initializer.getArguments()[0];

		assertThat(arg.getSeparator(), nullValue());
		assertThat(arg.getInit().getType(), is(AssignmentOperator.ASSIGN));
		assertThat(arg.getValue(), isName("baz"));
	}

	private DeclaratorNode parse(String text) {
		this.worker = new ParserWorker(
				new StringSource(getClass().getSimpleName(), text));

		final MemberRefNode field =
				to(MemberRefNode.class, this.worker.parse(ref()));

		assertThat(field, isName("foo"));

		return this.worker.parse(declarator(field));
	}

}

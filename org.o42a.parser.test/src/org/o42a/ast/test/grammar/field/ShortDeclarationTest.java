/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.field;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.expression.BracketsNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class ShortDeclarationTest extends GrammarTestCase {

	@Test
	public void adapter() {

		final DeclaratorNode declarator = parse("@foo [bar]");
		final DeclarableAdapterNode declarable =
				to(DeclarableAdapterNode.class, declarator.getDeclarable());

		assertThat(declarable.getMember(), isName("foo"));
		assertThat(declarator.getDefinitionAssignment(), nullValue());

		final PhraseNode phrase =
				to(PhraseNode.class, declarator.getDefinition());

		assertThat(
				to(ScopeRefNode.class, phrase.getPrefix()).getType(),
				is(ScopeType.IMPLIED));
		singlePhrasePart(BracketsNode.class, phrase);
	}

	@Test
	public void adapterWithoutDefinition() {

		final DeclaratorNode declarator = parse("@foo ~~ comment");
		final DeclarableAdapterNode declarable =
				to(DeclarableAdapterNode.class, declarator.getDeclarable());

		assertThat(declarable.getMember(), isName("foo"));
		assertThat(declarator.getDefinitionAssignment(), nullValue());
		assertThat(declarator.getDefinition(), nullValue());
	}

	@Test
	public void adapterWithoutDefinitionAtEOF() {

		final DeclaratorNode declarator = parse("@foo");
		final DeclarableAdapterNode declarable =
				to(DeclarableAdapterNode.class, declarator.getDeclarable());

		assertThat(declarable.getMember(), isName("foo"));
		assertThat(declarator.getDefinitionAssignment(), nullValue());
		assertThat(declarator.getDefinition(), nullValue());
	}

	@Test
	public void field() {

		final DeclaratorNode declarator = parse("*foo [bar]");
		final MemberRefNode declarable =
				to(MemberRefNode.class, declarator.getDeclarable());

		assertThat(declarable, hasName("foo"));
		assertThat(declarable, memberRefWithoutRetention());
		assertThat(
				to(ScopeRefNode.class, declarable.getOwner()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(declarator.getDefinitionAssignment(), nullValue());

		final PhraseNode phrase =
				to(PhraseNode.class, declarator.getDefinition());

		assertThat(
				to(ScopeRefNode.class, phrase.getPrefix()).getType(),
				is(ScopeType.IMPLIED));
		singlePhrasePart(BracketsNode.class, phrase);
	}

	@Test
	public void fieldWithoutDefinition() {

		final DeclaratorNode declarator = parse("*foo ~~ comment");
		final MemberRefNode declarable =
				to(MemberRefNode.class, declarator.getDeclarable());

		assertThat(declarable, hasName("foo"));
		assertThat(declarable, memberRefWithoutRetention());
		assertThat(
				to(ScopeRefNode.class, declarable.getOwner()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(declarator.getDefinitionAssignment(), nullValue());
		assertThat(declarator.getDefinition(), nullValue());
	}

	@Test
	public void fieldWithoutDefinitionAtEOF() {

		final DeclaratorNode declarator = parse("*foo");
		final MemberRefNode declarable =
				to(MemberRefNode.class, declarator.getDeclarable());

		assertThat(declarable, hasName("foo"));
		assertThat(declarable, memberRefWithoutRetention());
		assertThat(
				to(ScopeRefNode.class, declarable.getOwner()).getType(),
				is(ScopeType.IMPLIED));
		assertThat(declarator.getDefinitionAssignment(), nullValue());
		assertThat(declarator.getDefinition(), nullValue());
	}

	private DeclaratorNode parse(String text) {
		return to(DeclaratorNode.class, parse(DECLARATIVE.statement(), text));
	}

}

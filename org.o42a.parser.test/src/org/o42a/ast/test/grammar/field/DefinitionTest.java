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
package org.o42a.ast.test.grammar.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.o42a.parser.Grammar.declarator;
import static org.o42a.parser.Grammar.ref;

import org.junit.Test;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.test.grammar.GrammarTestCase;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.DefinitionKind;
import org.o42a.parser.ParserWorker;
import org.o42a.util.io.StringSource;


public class DefinitionTest extends GrammarTestCase {

	@Test
	public void link() {

		final DeclaratorNode result = parse("foo := `bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertNull(result.getDefinitionType());
		assertName("bar", result.getDefinition());
	}

	@Test
	public void staticLink() {

		final DeclaratorNode result = parse("foo := `&bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertNull(result.getDefinitionType());

		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getDefinition());

		assertFalse(ascendants.hasSamples());
		assertName("bar", ascendants.getAncestor().getSpec());
	}

	@Test
	public void linkInterface() {

		final DeclaratorNode result = parse("foo := (`bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());
		assertName("bar", result.getDefinitionType());
		assertName("baz", result.getDefinition());
	}

	@Test
	public void staticLinkInterface() {

		final DeclaratorNode result = parse("foo := (`&bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.LINK, result.getDefinitionKind());

		final AscendantsNode ascendants =
				to(AscendantsNode.class, result.getDefinitionType());

		assertName("bar", ascendants.getAncestor().getSpec());
		assertName("baz", result.getDefinition());
	}

	@Test
	public void variable() {

		final DeclaratorNode result = parse("foo := ``bar");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.VARIABLE, result.getDefinitionKind());
		assertNull(result.getDefinitionType());
		assertName("bar", result.getDefinition());
	}

	@Test
	public void variableInterface() {

		final DeclaratorNode result = parse("foo := (``bar) baz");

		assertEquals(DeclarationTarget.VALUE, result.getTarget());
		assertEquals(DefinitionKind.VARIABLE, result.getDefinitionKind());
		assertName("bar", result.getDefinitionType());
		assertName("baz", result.getDefinition());
	}

	private DeclaratorNode parse(String text) {
		this.worker = new ParserWorker(
				new StringSource(getClass().getSimpleName(), text));

		final MemberRefNode field =
				to(MemberRefNode.class, this.worker.parse(ref()));

		assertName("foo", field);

		return this.worker.parse(declarator(field));
	}

}

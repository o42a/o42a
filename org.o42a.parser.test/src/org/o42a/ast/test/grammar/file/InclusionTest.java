/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.file;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.o42a.parser.Grammar.DECLARATIVE;

import org.junit.Test;
import org.o42a.ast.file.InclusionNode;
import org.o42a.ast.test.grammar.GrammarTestCase;


public class InclusionTest extends GrammarTestCase {

	@Test
	public void inclusionWithoutLabel() {
		expectError("missing_inclusion_tag");

		final InclusionNode inclusion = parse("***");

		assertThat(inclusion, hasRange(0, 3));
		assertThat(inclusion.getPrefix().getType().getLength(), is(3));
		assertNull(inclusion.getTag());
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithoutSuffix() {

		final InclusionNode inclusion = parse("***** Label");

		assertThat(inclusion.getPrefix(), hasRange(0, 5));
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(canonicalName(inclusion.getTag()), is("label"));
		assertNull(inclusion.getSuffix());
	}

	@Test
	public void inclusionWithSuffix() {

		final InclusionNode inclusion = parse("***** Label **");

		assertThat(inclusion.getPrefix(), hasRange(0, 5));
		assertThat(inclusion.getPrefix().getType().getLength(), is(5));
		assertThat(canonicalName(inclusion.getTag()), is("label"));
		assertThat(inclusion.getSuffix().getType().getLength(), is(2));
	}

	private InclusionNode parse(String text) {
		return to(
				InclusionNode.class,
				parse(DECLARATIVE.statement(), text));
	}

}

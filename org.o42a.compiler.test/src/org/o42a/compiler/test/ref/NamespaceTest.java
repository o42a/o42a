/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class NamespaceTest extends CompilerTestCase {

	@Test
	public void objectAlias() {
		compile(
				"Use object 'used' as 'a'",
				"Used := 1",
				"User := a");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void nestedObjectAlias() {
		compile(
				"Use object 'ns: used' as 'a'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := a");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void useObject() {
		compile(
				"Use object 'ns: used'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := used");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

	@Test
	public void sameNamespace() {
		compile(
				"Use namespace _ object 'ns'",
				"Ns := void(",
				"  Used := 1",
				")",
				"User := used");

		assertThat(definiteValue(field("user"), ValueType.INTEGER), is(1L));
	}

}

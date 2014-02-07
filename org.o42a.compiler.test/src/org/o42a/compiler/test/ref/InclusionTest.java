/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.ValueType;


public class InclusionTest extends CompilerTestCase {

	@Test
	public void fieldInclusion() {
		addSource(
				"included",
				"Included := integer",
				"===================",
				"= 42");
		compile("");

		final Field included = field("included");

		assertThat(definiteValue(included, ValueType.INTEGER), is(42L));
	}

}

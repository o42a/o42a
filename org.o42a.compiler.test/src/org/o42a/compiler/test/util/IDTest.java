/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.junit.Test;
import org.o42a.util.string.ID;


public class IDTest {

	@Test
	public void decaptialization() {

		final ID id =
				caseInsensitiveName("Abc").toID()
				.sub(caseInsensitiveName("Def"))
				.sub(caseInsensitiveName("GHI"));

		assertThat(id.toString(), is("Abc: def: GHI"));
	}

}

/*
    Compiler Tests
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.compiler.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.o42a.util.use.User.useCase;

import org.junit.Before;
import org.junit.Test;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.Useable;
import org.o42a.util.use.User;


public class UseTest {

	private TestUseable useable;
	private UseCase useCase;

	@Before
	public void createUseable() {
		this.useable = new TestUseable();
		this.useCase = useCase("Use Case");
	}

	@Test
	public void notUsed() {
		assertFalse(this.useable.usedBy(this.useCase));
	}

	@Test
	public void used() {
		this.useable.useBy(this.useCase);
		assertTrue(this.useable.usedBy(this.useCase));
	}

	@Test
	public void transitiveUse() {

		final TestUseable useable2 = new TestUseable();

		this.useable.useBy(useable2);
		useable2.useBy(this.useCase);

		assertTrue(useable2.usedBy(this.useCase));
		assertTrue(this.useable.usedBy(this.useCase));
	}

	@Test
	public void useIsNotBeingUsed() {

		final TestUseable useable2 = new TestUseable();

		this.useable.useBy(useable2);
		this.useable.useBy(this.useCase);

		assertFalse(useable2.usedBy(this.useCase));
		assertTrue(this.useable.usedBy(this.useCase));
	}

	@Test
	public void deepUse() {

		final TestUseable useable2 = new TestUseable();
		final TestUseable useable3 = new TestUseable();
		final TestUseable useable4 = new TestUseable();
		final TestUseable useable5 = new TestUseable();

		this.useable.useBy(useable2);
		useable2.useBy(useable3);
		useable3.useBy(useable4);
		useable4.useBy(useable5);
		useable5.useBy(this.useCase);

		assertTrue(useable2.usedBy(this.useCase));
		assertTrue(useable3.usedBy(this.useCase));
		assertTrue(useable4.usedBy(this.useCase));
		assertTrue(useable5.usedBy(this.useCase));
		assertTrue(this.useable.usedBy(this.useCase));
	}

	private static final class TestUseable extends Useable<Integer> {

		private int counter;

		@Override
		protected Integer createUsed(User user) {
			return ++this.counter;
		}

	}

}

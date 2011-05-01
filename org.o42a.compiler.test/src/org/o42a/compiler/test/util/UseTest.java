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
import org.o42a.util.use.Usable;
import org.o42a.util.use.UseCase;
import org.o42a.util.use.User;


public class UseTest {

	private TestUsable usable;
	private UseCase useCase;
	private int usableIdSeq;

	@Before
	public void createUseable() {
		this.usableIdSeq = 0;
		this.usable = new TestUsable();
		this.useCase = useCase("Use Case");
	}

	@Test
	public void notUsed() {
		assertFalse(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void used() {
		this.usable.useBy(this.useCase);
		assertTrue(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void transitiveUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.useCase);

		assertTrue(usable2.isUsedBy(this.useCase));
		assertTrue(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void useIsNotBeingUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		this.usable.useBy(this.useCase);

		assertFalse(usable2.isUsedBy(this.useCase));
		assertTrue(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void deepUse() {

		final TestUsable usable2 = new TestUsable();
		final TestUsable usable3 = new TestUsable();
		final TestUsable usable4 = new TestUsable();
		final TestUsable usable5 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(usable3);
		usable3.useBy(usable4);
		usable4.useBy(usable5);
		usable5.useBy(this.useCase);

		assertTrue(usable2.isUsedBy(this.useCase));
		assertTrue(usable3.isUsedBy(this.useCase));
		assertTrue(usable4.isUsedBy(this.useCase));
		assertTrue(usable5.isUsedBy(this.useCase));
		assertTrue(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void selfNotUsed() {
		this.usable.useBy(this.usable);

		assertFalse(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void selfUse() {
		this.usable.useBy(this.usable);
		this.usable.useBy(this.useCase);

		assertTrue(this.usable.isUsedBy(this.useCase));
	}

	@Test
	public void recurrentNotUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);

		assertFalse(this.usable.isUsedBy(this.useCase));
		assertFalse(usable2.isUsedBy(this.useCase));
	}

	@Test
	public void recurrentUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);
		usable2.useBy(this.useCase);

		assertTrue(this.usable.isUsedBy(this.useCase));
		assertTrue(usable2.isUsedBy(this.useCase));
	}

	@Test
	public void recurrentUse2() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);
		this.usable.useBy(this.useCase);

		assertTrue(this.usable.isUsedBy(this.useCase));
		assertTrue(usable2.isUsedBy(this.useCase));
	}

	@Test
	public void recurrentUse7() {

		final TestUsable usable2 = new TestUsable();
		final TestUsable usable3 = new TestUsable();
		final TestUsable usable4 = new TestUsable();
		final TestUsable usable5 = new TestUsable();
		final TestUsable usable6 = new TestUsable();
		final TestUsable usable7 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(usable3);
		usable3.useBy(usable4);
		usable4.useBy(usable5);
		usable5.useBy(usable6);
		usable6.useBy(usable7);
		usable7.useBy(this.usable);
		usable4.useBy(this.useCase);

		assertTrue(this.usable.isUsedBy(this.useCase));
		assertTrue(usable2.isUsedBy(this.useCase));
		assertTrue(usable3.isUsedBy(this.useCase));
		assertTrue(usable4.isUsedBy(this.useCase));
		assertTrue(usable5.isUsedBy(this.useCase));
		assertTrue(usable6.isUsedBy(this.useCase));
		assertTrue(usable7.isUsedBy(this.useCase));
	}

	private final class TestUsable extends Usable<Integer> {

		private final String id;
		private int counter;

		TestUsable() {
			this.id = "Usable" + (++UseTest.this.usableIdSeq);
		}

		@Override
		public String toString() {
			return this.id;
		}

		@Override
		protected Integer createUsed(User user) {
			return ++this.counter;
		}

	}

}

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
import org.o42a.util.use.*;


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
		assertUnused(this.usable);
	}

	@Test
	public void used() {
		this.usable.useBy(this.useCase);
		assertUsed(this.usable);
	}

	@Test
	public void transitiveUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.useCase);

		assertUsed(usable2);
		assertUsed(this.usable);
	}

	@Test
	public void useIsNotBeingUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		this.usable.useBy(this.useCase);

		assertUnused(usable2);
		assertUsed(this.usable);
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

		assertUsed(usable2);
		assertUsed(usable3);
		assertUsed(usable4);
		assertUsed(usable5);
		assertUsed(this.usable);
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

		assertUsed(this.usable);
	}

	@Test
	public void recurrentNotUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);

		assertUnused(this.usable);
		assertUnused(usable2);
	}

	@Test
	public void recurrentUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);
		usable2.useBy(this.useCase);

		assertUsed(this.usable);
		assertUsed(usable2);
	}

	@Test
	public void recurrentUse2() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2);
		usable2.useBy(this.usable);
		this.usable.useBy(this.useCase);

		assertUsed(this.usable);
		assertUsed(usable2);
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

		assertUsed(this.usable);
		assertUsed(usable2);
		assertUsed(usable3);
		assertUsed(usable4);
		assertUsed(usable5);
		assertUsed(usable6);
		assertUsed(usable7);
	}

	public void assertUsed(UseInfo use) {
		assertTrue(
				use + " is not used by " + this.useCase,
				use.getUseBy(this.useCase).isUsed());
	}

	public void assertUnused(UseInfo use) {
		assertFalse(
				use + " is used by " + this.useCase,
				use.getUseBy(this.useCase).isUsed());
	}

	private final class TestUsable extends Usable<Integer> {

		private final String id;
		private final UsableUser user;
		private int counter;

		TestUsable() {
			this.id = "Usable" + (++UseTest.this.usableIdSeq);
			this.user = new UsableUser(this);
		}

		@Override
		public final User toUser() {
			return this.user;
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

/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.User.useCase;

import org.junit.Before;
import org.junit.Test;
import org.o42a.analysis.use.*;


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
	public void unused() {
		assertUnused(this.usable);
	}

	@Test
	public void used() {
		this.usable.useBy(this.useCase, SIMPLE_USAGE);
		assertUsed(this.usable);
	}

	@Test
	public void updateAfterCheck() {
		assertUnused(this.usable);

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);

		assertUnused(this.usable);

		usable2.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(this.usable);
	}

	@Test
	public void transitiveUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(usable2);
		assertUsed(this.usable);
	}

	@Test
	public void useIsNotBeingUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		this.usable.useBy(this.useCase, SIMPLE_USAGE);

		assertUnused(usable2);
		assertUsed(this.usable);
	}

	@Test
	public void deepUse() {

		final TestUsable usable2 = new TestUsable();
		final TestUsable usable3 = new TestUsable();
		final TestUsable usable4 = new TestUsable();
		final TestUsable usable5 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(usable3, SIMPLE_USAGE);
		usable3.useBy(usable4, SIMPLE_USAGE);
		usable4.useBy(usable5, SIMPLE_USAGE);
		usable5.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(usable2);
		assertUsed(usable3);
		assertUsed(usable4);
		assertUsed(usable5);
		assertUsed(this.usable);
	}

	@Test
	public void selfNotUsed() {
		this.usable.useBy(this.usable, SIMPLE_USAGE);

		assertFalse(this.usable.isUsed(this.useCase, SIMPLE_USAGE));
	}

	@Test
	public void selfUse() {
		this.usable.useBy(this.usable, SIMPLE_USAGE);
		this.usable.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(this.usable);
	}

	@Test
	public void recurrentNotUsed() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(this.usable, SIMPLE_USAGE);

		assertUnused(this.usable);
		assertUnused(usable2);
	}

	@Test
	public void recurrentUse() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(this.usable, SIMPLE_USAGE);
		usable2.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(this.usable);
		assertUsed(usable2);
	}

	@Test
	public void recurrentUse2() {

		final TestUsable usable2 = new TestUsable();

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(this.usable, SIMPLE_USAGE);
		this.usable.useBy(this.useCase, SIMPLE_USAGE);

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

		this.usable.useBy(usable2, SIMPLE_USAGE);
		usable2.useBy(usable3, SIMPLE_USAGE);
		usable3.useBy(usable4, SIMPLE_USAGE);
		usable4.useBy(usable5, SIMPLE_USAGE);
		usable5.useBy(usable6, SIMPLE_USAGE);
		usable6.useBy(usable7, SIMPLE_USAGE);
		usable7.useBy(this.usable, SIMPLE_USAGE);
		usable4.useBy(this.useCase, SIMPLE_USAGE);

		assertUsed(this.usable);
		assertUsed(usable2);
		assertUsed(usable3);
		assertUsed(usable4);
		assertUsed(usable5);
		assertUsed(usable6);
		assertUsed(usable7);
	}

	public void assertUsed(Uses<SimpleUsage> use) {
		assertTrue(
				use + " is not used by " + this.useCase,
				use.selectUse(this.useCase, ALL_SIMPLE_USAGES).isUsed());
	}

	public void assertUnused(Uses<SimpleUsage> use) {
		assertFalse(
				use + " is used by " + this.useCase,
				use.selectUse(this.useCase, ALL_SIMPLE_USAGES).isUsed());
	}

	private final class TestUsable extends Usable<SimpleUsage> {

		private final String id;
		private final UsableUser<SimpleUsage> user;

		TestUsable() {
			super(ALL_SIMPLE_USAGES);
			this.id = "Usable" + (++UseTest.this.usableIdSeq);
			this.user = new UsableUser<SimpleUsage>(this);
		}

		@Override
		public final User<SimpleUsage> toUser() {
			return this.user;
		}

		@Override
		public String toString() {
			return this.id;
		}

	}

}

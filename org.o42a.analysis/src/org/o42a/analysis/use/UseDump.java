/*
    Compilation Analysis
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.analysis.use;

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;

import java.util.ArrayList;


/**
 * Use tracking dumper.
 *
 * <p>This class is only used for debugging purposes.</p>
 *
 * @param <U> usage.
 */
public class UseDump<U extends Usage<U>> {

	private static final ArrayList<UseDump<?>> uses = new ArrayList<>();
	private static boolean enabled = true;

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		UseDump.enabled = enabled;
	}

	public static <U extends Usage<U>> void dumpUse(
			Object what,
			User<U> user,
			U usage) {
		if (!isEnabled()) {
			return;
		}

		final UseDump<U> dump = new UseDump<>(what.toString(), user, usage);

		uses.add(dump);
	}

	public static void dumpSeparator(String what) {
		if (!isEnabled()) {
			return;
		}
		uses.add(new Separator(what));
	}

	public static void printUses(UseCaseInfo useCase) {
		for (UseDump<?> dump : uses) {
			dump.print(useCase);
		}
	}

	protected final String what;
	protected final User<U> user;
	protected final U usage;

	UseDump(String what, User<U> user, U usage) {
		this.what = what;
		this.user = user;
		this.usage = usage;
	}

	public void print(UseCaseInfo useCase) {
		if (this.user.isUsed(useCase, this.usage)) {
			System.err.println("(!) " + this.what);
			System.err.println("  + " + this.user);
		}
	}

	@Override
	public String toString() {
		return this.what + " by " + this.user;
	}

	private static final class Separator extends UseDump<SimpleUsage> {

		Separator(String what) {
			super(what, null, SIMPLE_USAGE);
		}

		@Override
		public void print(UseCaseInfo useCase) {
			System.err.println("(!) " + this.what);
		}

		@Override
		public String toString() {
			return this.what;
		}

	}

}

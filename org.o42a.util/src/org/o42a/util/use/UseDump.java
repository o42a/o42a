/*
    Utilities
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
package org.o42a.util.use;

import java.util.ArrayList;



/**
 * Use tracking dumper.
 *
 * <p>This class is only used for debugging purposes.</p>
 */
public class UseDump {

	private static final ArrayList<UseDump> uses = new ArrayList<UseDump>();

	public static void dumpUse(Object what, UserInfo user) {
		uses.add(new UseDump(what.toString(), user.toUser()));
	}

	public static void dumpSeparator(String what) {
		uses.add(new Separator(what));
	}

	public static void printUses(UseCaseInfo useCase) {
		for (UseDump dump : uses) {
			dump.print(useCase);
		}
	}

	protected final String what;
	protected final User user;

	UseDump(String what, User user) {
		this.what = what;
		this.user = user;
	}

	public void print(UseCaseInfo useCase) {
		if (this.user.isUsedBy(useCase)) {
			System.err.println("(!) " + this.what);
			System.err.println("  + " + this.user);
		}
	}

	@Override
	public String toString() {
		return this.what + " by " + this.user;
	}

	private static final class Separator extends UseDump {

		Separator(String what) {
			super(what, null);
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

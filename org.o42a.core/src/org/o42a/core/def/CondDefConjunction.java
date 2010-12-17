/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.core.ref.Cond;


final class CondDefConjunction {

	public static CondDefConjunction conjunction(
			SingleCondDef[] conds,
			CondDef cond,
			boolean reverse) {
		return conjunction(conds, 0, conds.length, cond, reverse);
	}

	public static CondDefConjunction conjunction(
			SingleCondDef[] conds,
			int from,
			int to,
			CondDef cond,
			boolean reverse) {

		final int step = reverse ? -1 : 1;
		final int start = reverse ? to - 1 : from;

		for (int i = start; i >= from && i < to; i += step) {

			final SingleCondDef conjunction;

			if (reverse) {
				conjunction = simpleConjunction(conds[i], cond);
			} else {
				conjunction = simpleConjunction(cond, conds[i]);
			}

			if (conjunction != null) {
				return new CondDefConjunction(conjunction, i);
			}
		}

		return null;
	}

	public static SingleCondDef simpleConjunction(
			CondDef cond1,
			CondDef cond2) {

		final Cond conjunction = condConjunction(cond1, cond2);

		if (conjunction == null) {
			return null;
		}

		return new SingleCondDef(
				cond1.getSource(),
				conjunction,
				cond1.getRescoper());
	}

	private static Cond condConjunction(CondDef cond1, CondDef cond2) {
		if (cond1.getSource() != cond2.getSource()) {
			return null;
		}
		if (!cond1.getRescoper().equals(cond2.getRescoper())) {
			return null;
		}
		return cond1.condition().and(cond2.condition());
	}

	private final SingleCondDef conjunction;
	private final int index;

	public CondDefConjunction(SingleCondDef conjunction, int index) {
		this.conjunction = conjunction;
		this.index = index;
	}

	public final SingleCondDef getConjunction() {
		return this.conjunction;
	}

	public final int getIndex() {
		return this.index;
	}

	@Override
	public String toString() {
		return this.conjunction + "#" + this.index;
	}

}

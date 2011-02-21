/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.core.ref.Logical;


final class LogicalDefConjunction {

	public static LogicalDefConjunction conjunction(
			SingleLogicalDef[] defs,
			LogicalDef def,
			boolean reverse) {
		return conjunction(defs, 0, defs.length, def, reverse);
	}

	public static LogicalDefConjunction conjunction(
			SingleLogicalDef[] defs,
			int from,
			int to,
			LogicalDef def,
			boolean reverse) {

		final int step = reverse ? -1 : 1;
		final int start = reverse ? to - 1 : from;

		for (int i = start; i >= from && i < to; i += step) {

			final SingleLogicalDef conjunction;

			if (reverse) {
				conjunction = simpleConjunction(defs[i], def);
			} else {
				conjunction = simpleConjunction(def, defs[i]);
			}

			if (conjunction != null) {
				return new LogicalDefConjunction(conjunction, i);
			}
		}

		return null;
	}

	public static SingleLogicalDef simpleConjunction(
			LogicalDef cond1,
			LogicalDef cond2) {

		final Logical conjunction = condConjunction(cond1, cond2);

		if (conjunction == null) {
			return null;
		}

		return new SingleLogicalDef(
				cond1.getSource(),
				conjunction,
				cond1.getRescoper());
	}

	private static Logical condConjunction(LogicalDef cond1, LogicalDef cond2) {
		if (cond1.getSource() != cond2.getSource()) {
			return null;
		}
		if (!cond1.getRescoper().equals(cond2.getRescoper())) {
			return null;
		}
		return cond1.getScoped().and(cond2.getScoped());
	}

	private final SingleLogicalDef conjunction;
	private final int index;

	public LogicalDefConjunction(SingleLogicalDef conjunction, int index) {
		this.conjunction = conjunction;
		this.index = index;
	}

	public final SingleLogicalDef getConjunction() {
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

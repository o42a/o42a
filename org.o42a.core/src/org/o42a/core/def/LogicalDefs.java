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

import static java.lang.System.arraycopy;
import static org.o42a.core.def.LogicalDefConjunction.conjunction;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.common.AbstractConjunction;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


final class LogicalDefs extends LogicalDef {

	LogicalDefs(SingleLogicalDef[] requirements) {
		super(
				requirements[0].getSource(),
				requirements[0].getLogical(),
				requirements[0].getRescoper(),
				requirements);
	}

	@Override
	public void writeFullLogical(Code code, CodePos exit, HostOp host) {
		code.debug("Full logical: " + this);
		for (SingleLogicalDef req : requirements()) {
			req.writeFullLogical(code, exit, host);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');
		for (LogicalDef prereq : getRequirements()) {
			out.append(" & ").append(prereq);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected LogicalDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {

		final SingleLogicalDef[] requirements = requirements();
		final SingleLogicalDef[] newRequirements =
			new SingleLogicalDef[requirements.length];

		for (int i = 0; i < requirements.length; ++i) {
			newRequirements[i] =
				requirements[i].rescope(additionalRescoper);
		}

		return new LogicalDefs(newRequirements);
	}

	@Override
	protected Logical createFullLogical() {
		return new FullLogical(this);
	}

	@Override
	protected LogicalDef conjunctionWith(LogicalDef requirement) {

		final SingleLogicalDef[] reqs1 = requirements();
		final SingleLogicalDef[] reqs2 = requirement.requirements();

		if (reqs2.length == 1) {

			final LogicalDefConjunction conjunction =
				conjunction(reqs1, requirement, true);

			if (conjunction != null) {

				final SingleLogicalDef[] newReqs = reqs1.clone();

				newReqs[conjunction.getIndex()] = conjunction.getConjunction();

				return new LogicalDefs(newReqs);
			}
		}

		final SingleLogicalDef[] newReqs =
			new SingleLogicalDef[reqs1.length + reqs2.length];

		arraycopy(reqs1, 0, newReqs, 0, reqs1.length);

		int idx = reqs1.length;

		for (int i = 0; i < reqs2.length; ++i) {

			final SingleLogicalDef req = reqs2[i];
			final LogicalDefConjunction conjunction =
				conjunction(newReqs, 0, reqs1.length, requirement, true);

			if (conjunction == null) {
				newReqs[idx++] = req;
				continue;
			}
			newReqs[conjunction.getIndex()] = conjunction.getConjunction();
		}

		return new LogicalDefs(ArrayUtil.clip(newReqs, idx));
	}

	private static final class FullLogical extends AbstractConjunction {

		private final LogicalDefs defs;

		FullLogical(LogicalDefs def) {
			super(def, def.getScope());
			this.defs = def;
		}

		@Override
		public LogicalDef toLogicalDef() {
			return this.defs;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.defs.writeFullLogical(code, exit, host);
		}

		@Override
		public String toString() {
			return this.defs.toString();
		}

		@Override
		protected int numClaims() {
			return this.defs.requirements().length;
		}

		@Override
		protected Logical claim(int index) {
			return this.defs.requirements()[index].getLogical();
		}

	}

}

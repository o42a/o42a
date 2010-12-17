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

import static java.lang.System.arraycopy;
import static org.o42a.core.def.CondDefConjunction.conjunction;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Cond;
import org.o42a.core.ref.common.AbstractConjunction;
import org.o42a.core.st.Reproducer;
import org.o42a.util.ArrayUtil;


final class CondDefs extends CondDef {

	CondDefs(SingleCondDef[] requirements) {
		super(
				requirements[0].getSource(),
				requirements[0].condition(),
				requirements[0].getRescoper(),
				requirements);
	}

	@Override
	public void writeFullCondition(Code code, CodePos exit, HostOp host) {
		code.debug("Full cond: " + this);
		for (SingleCondDef req : requirements()) {
			req.writeFullCondition(code, exit, host);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');
		for (CondDef prereq : getRequirements()) {
			out.append(" & ").append(prereq);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected CondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {

		final SingleCondDef[] requirements = requirements();
		final SingleCondDef[] newRequirements =
			new SingleCondDef[requirements.length];

		for (int i = 0; i < requirements.length; ++i) {
			newRequirements[i] =
				requirements[i].rescope(additionalRescoper);
		}

		return new CondDefs(newRequirements);
	}

	@Override
	protected Cond createFullCondition() {
		return new FullCondition(this);
	}

	@Override
	protected CondDef conjunctionWith(CondDef requirement) {

		final SingleCondDef[] reqs1 = requirements();
		final SingleCondDef[] reqs2 = requirement.requirements();

		if (reqs2.length == 1) {

			final CondDefConjunction conjunction =
				conjunction(reqs1, requirement, true);

			if (conjunction != null) {

				final SingleCondDef[] newReqs = reqs1.clone();

				newReqs[conjunction.getIndex()] = conjunction.getConjunction();

				return new CondDefs(newReqs);
			}
		}

		final SingleCondDef[] newReqs =
			new SingleCondDef[reqs1.length + reqs2.length];

		arraycopy(reqs1, 0, newReqs, 0, reqs1.length);

		int idx = reqs1.length;

		for (int i = 0; i < reqs2.length; ++i) {

			final SingleCondDef req = reqs2[i];
			final CondDefConjunction conjunction =
				conjunction(newReqs, 0, reqs1.length, requirement, true);

			if (conjunction == null) {
				newReqs[idx++] = req;
				continue;
			}
			newReqs[conjunction.getIndex()] = conjunction.getConjunction();
		}

		return new CondDefs(ArrayUtil.clip(newReqs, idx));
	}

	private static final class FullCondition extends AbstractConjunction {

		private final CondDefs defs;

		FullCondition(CondDefs def) {
			super(def, def.getScope());
			this.defs = def;
		}

		@Override
		public CondDef toCondDef() {
			return this.defs;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.defs.writeFullCondition(code, exit, host);
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
		protected Cond claim(int index) {
			return this.defs.requirements()[index].condition();
		}

	}

}

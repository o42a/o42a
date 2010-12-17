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

import static org.o42a.core.def.CondDefConjunction.conjunction;
import static org.o42a.core.def.CondDefConjunction.simpleConjunction;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class SingleCondDef extends CondDef {

	SingleCondDef(Obj source, Cond condition, Rescoper rescoper) {
		super(source, condition, rescoper);
	}

	@Override
	public SingleCondDef rescope(Rescoper rescoper) {
		return (SingleCondDef) super.rescope(rescoper);
	}

	@Override
	public void writeFullCondition(Code code, CodePos exit, HostOp host) {
		code.debug("Full cond: " + this);
		writeCondition(code, exit, host);
	}

	@Override
	public String toString() {
		return condition().toString();
	}

	@Override
	protected Cond createFullCondition() {

		final Cond condition = condition();

		if (getScope() == condition.getScope()) {
			return condition;
		}

		return new FullCondition(this);
	}

	@Override
	protected SingleCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new SingleCondDef(getSource(), condition(), rescoper);
	}

	@Override
	protected CondDef conjunctionWith(CondDef requirement) {

		final SingleCondDef[] requirements = requirement.requirements();

		if (requirements.length == 1) {

			final SingleCondDef conjunction =
				simpleConjunction(this, requirement);

			if (conjunction != null) {
				return conjunction;
			}
		}

		final CondDefConjunction conjunction =
			conjunction(requirements, this, false);

		if (conjunction == null) {

			final SingleCondDef[] newReqs =
				new SingleCondDef[1 + requirements.length];

			newReqs[0] = this;
			System.arraycopy(
					requirements,
					0,
					newReqs,
					1,
					requirements.length);

			return new CondDefs(newReqs);
		}

		final SingleCondDef[] newReqs = requirements.clone();

		newReqs[conjunction.getIndex()] = conjunction.getConjunction();

		return new CondDefs(newReqs);
	}

	private static final class FullCondition extends Cond {

		private final SingleCondDef def;

		FullCondition(SingleCondDef def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.def.condition().getConstantValue();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return this.def.condition().logicalValue(
					this.def.getRescoper().rescope(scope));
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.def.writeFullCondition(code, exit, host);
		}

		@Override
		public CondDef toCondDef() {
			return this.def;
		}

		@Override
		public String toString() {
			return this.def.toString();
		}

	}

}

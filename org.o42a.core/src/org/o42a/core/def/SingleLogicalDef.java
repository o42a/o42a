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

import static org.o42a.core.def.LogicalDefConjunction.conjunction;
import static org.o42a.core.def.LogicalDefConjunction.simpleConjunction;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class SingleLogicalDef extends LogicalDef {

	SingleLogicalDef(Obj source, Logical logical, Rescoper rescoper) {
		super(source, logical, rescoper);
	}

	@Override
	public SingleLogicalDef rescope(Rescoper rescoper) {
		return (SingleLogicalDef) super.rescope(rescoper);
	}

	@Override
	public void writeFullLogical(Code code, CodePos exit, HostOp host) {
		code.debug("Full logical: " + this);
		writeLogical(code, exit, host);
	}

	@Override
	public String toString() {
		return getScoped().toString();
	}

	@Override
	protected Logical createFullLogical() {

		final Logical logical = getScoped();

		if (getScope() == logical.getScope()) {
			return logical;
		}

		return new FullLogical(this);
	}

	@Override
	protected SingleLogicalDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new SingleLogicalDef(getSource(), getScoped(), rescoper);
	}

	@Override
	protected LogicalDef conjunctionWith(LogicalDef requirement) {

		final SingleLogicalDef[] requirements = requirement.requirements();

		if (requirements.length == 1) {

			final SingleLogicalDef conjunction =
				simpleConjunction(this, requirement);

			if (conjunction != null) {
				return conjunction;
			}
		}

		final LogicalDefConjunction conjunction =
			conjunction(requirements, this, false);

		if (conjunction == null) {

			final SingleLogicalDef[] newReqs =
				new SingleLogicalDef[1 + requirements.length];

			newReqs[0] = this;
			System.arraycopy(
					requirements,
					0,
					newReqs,
					1,
					requirements.length);

			return new LogicalDefs(newReqs);
		}

		final SingleLogicalDef[] newReqs = requirements.clone();

		newReqs[conjunction.getIndex()] = conjunction.getConjunction();

		return new LogicalDefs(newReqs);
	}

	private static final class FullLogical extends Logical {

		private final SingleLogicalDef def;

		FullLogical(SingleLogicalDef def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return this.def.getScoped().getConstantValue();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return this.def.getScoped().logicalValue(
					this.def.getRescoper().rescope(scope));
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			this.def.writeFullLogical(code, exit, host);
		}

		@Override
		public LogicalDef toLogicalDef() {
			return this.def;
		}

		@Override
		public String toString() {
			return this.def.toString();
		}

	}

}

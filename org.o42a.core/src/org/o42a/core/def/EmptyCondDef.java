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

import static org.o42a.core.def.Def.sourceOf;
import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Cond.trueCondition;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class EmptyCondDef extends CondDef {

	private static final SingleCondDef[] NO_REQUIREMENTS =
		new SingleCondDef[0];

	EmptyCondDef(LocationSpec location, Scope scope) {
		super(
				sourceOf(scope),
				trueCondition(location, scope),
				transparentRescoper(scope),
				NO_REQUIREMENTS);
	}

	private EmptyCondDef(EmptyCondDef prototype, Rescoper rescoper) {
		super(
				prototype.getSource(),
				prototype.condition(),
				rescoper,
				NO_REQUIREMENTS);
	}

	@Override
	public void writeFullCondition(Code code, CodePos exit, HostOp host) {
		code.debug("Full cond: " + this);
	}

	@Override
	public boolean sameAs(CondDef other) {
		return other.isEmpty();
	}

	@Override
	public String toString() {
		return "EMPTY";
	}

	@Override
	protected Cond createFullCondition() {
		return new FullCondition(this);
	}

	@Override
	protected EmptyCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new EmptyCondDef(this, rescoper);
	}

	@Override
	protected CondDef conjunctionWith(CondDef requirement) {
		return requirement;
	}

	private static final class FullCondition extends Cond {

		private final CondDef def;

		FullCondition(CondDef def) {
			super(def, def.getScope());
			this.def = def;
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.TRUE;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return LogicalValue.TRUE;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
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

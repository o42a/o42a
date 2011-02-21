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

import static org.o42a.core.def.Def.sourceOf;
import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class EmptyLogicalDef extends LogicalDef {

	private static final SingleLogicalDef[] NO_REQUIREMENTS =
		new SingleLogicalDef[0];

	EmptyLogicalDef(LocationInfo location, Scope scope) {
		super(
				sourceOf(scope),
				logicalTrue(location, scope),
				transparentRescoper(scope),
				NO_REQUIREMENTS);
	}

	private EmptyLogicalDef(EmptyLogicalDef prototype, Rescoper rescoper) {
		super(
				prototype.getSource(),
				prototype.getScoped(),
				rescoper,
				NO_REQUIREMENTS);
	}

	@Override
	public void writeFullLogical(Code code, CodePos exit, HostOp host) {
		code.debug("Full logical: " + this);
	}

	@Override
	public boolean sameAs(LogicalDef other) {
		return other.isEmpty();
	}

	@Override
	public String toString() {
		return "EMPTY";
	}

	@Override
	protected Logical createFullLogical() {
		return new FullLogical(this);
	}

	@Override
	protected EmptyLogicalDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new EmptyLogicalDef(this, rescoper);
	}

	@Override
	protected LogicalDef conjunctionWith(LogicalDef requirement) {
		return requirement;
	}

	private static final class FullLogical extends Logical {

		private final LogicalDef def;

		FullLogical(LogicalDef def) {
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
		public Logical reproduce(Reproducer reproducer) {
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
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

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

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class RefValueDef extends ValueDef {

	private final Ref ref;

	RefValueDef(Ref ref) {
		super(sourceOf(ref), ref, transparentRescoper(ref.getScope()));
		this.ref = ref;
	}

	RefValueDef(RefValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.ref = prototype.ref;
	}

	@Override
	public ValueType<?> getValueType() {
		return this.ref.getValueType();
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return this.ref.value(scope);
	}

	@Override
	protected RefValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new RefValueDef(this, rescoper);
	}

	@Override
	protected void fullyResolve() {
		this.ref.resolveAll();
		getRescoper().resolveAll();
	}

	@Override
	protected void writeValue(CodeDirs dirs, HostOp host, ValOp result) {
		this.ref.op(host).writeValue(dirs, result);
	}

}

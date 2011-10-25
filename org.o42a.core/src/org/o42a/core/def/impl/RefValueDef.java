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
package org.o42a.core.def.impl;

import static org.o42a.core.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Rescoper;
import org.o42a.core.Scope;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class RefValueDef extends ValueDef {

	private final Ref ref;

	public RefValueDef(Ref ref) {
		super(sourceOf(ref), ref, transparentRescoper(ref.getScope()));
		this.ref = ref;
	}

	private RefValueDef(RefValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.ref = prototype.ref;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final Scope scope = getRescoper().rescope(getScope());

		return this.ref.valueStruct(scope).rescope(getRescoper());
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
	protected boolean hasConstantValue() {
		return this.ref.isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.ref.value(resolver);
	}

	@Override
	protected RefValueDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new RefValueDef(this, rescoper);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolveValues(resolver);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.ref.op(host).writeValue(dirs);
	}

}

/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;

import org.o42a.core.Scope;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;


public final class ArrayCopyValueDef extends ValueDef {

	private final Ref ref;
	private final boolean toConstant;
	private ArrayValueStruct fromStruct;
	private ArrayValueStruct toStruct;

	public ArrayCopyValueDef(Ref ref, boolean toConstant) {
		super(sourceOf(ref), ref, transparentRescoper(ref.getScope()));
		this.ref = ref;
		this.toConstant = toConstant;
	}

	private ArrayCopyValueDef(ArrayCopyValueDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.ref = prototype.ref;
		this.toConstant = prototype.toConstant;
	}

	@Override
	public ArrayValueStruct getValueStruct() {
		if (this.toStruct != null) {
			return this.toStruct;
		}

		final ArrayValueStruct fromStruct = fromValueStruct();

		return this.toStruct = new ArrayValueStruct(
				fromStruct.getItemTypeRef(),
				this.toConstant);
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
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
		return runtimeLogical(this, this.ref.getScope());
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return getValueStruct().runtimeValue();
	}

	@Override
	protected CondDef createCondDef() {
		return this.ref.toCondDef();
	}

	@Override
	protected ValueDef create(Rescoper rescoper, Rescoper additionalRescoper) {
		return new ArrayCopyValueDef(this, rescoper);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolveValues(resolver);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		// TODO Auto-generated method stub
		return null;
	}

	private final ArrayValueStruct fromValueStruct() {
		if (this.fromStruct != null) {
			return this.fromStruct;
		}

		final Scope scope = getRescoper().rescope(getScope());

		return this.fromStruct =
				(ArrayValueStruct) this.ref.valueStruct(scope).rescope(
						getRescoper());
	}

}

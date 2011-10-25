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

import static org.o42a.core.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.core.Rescoper;
import org.o42a.core.Scope;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;


final class ArrayCopyValueDef extends ValueDef {

	static Value<?> arrayValue(
			Ref ref,
			Resolver resolver,
			boolean toConstant) {

		final Resolution arrayResolution = ref.resolve(resolver);

		if (arrayResolution.isError()) {
			return falseValue();
		}

		final Obj arrayObject = arrayResolution.materialize();
		final Value<?> value =
				arrayObject.value().explicitUseBy(resolver).getValue();
		final ArrayValueStruct sourceStruct =
				(ArrayValueStruct) value.getValueStruct();
		final ArrayValueStruct resultStruct =
				sourceStruct.setConstant(toConstant);

		if (value.isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.isDefinite()) {
			return resultStruct.runtimeValue();
		}
		if (!sourceStruct.isConstant()) {
			// Non-constant array can not be copied at compile time.
			return resultStruct.runtimeValue();
		}

		final Rescoper rescoper = ref.toRescoper();
		final Array array = sourceStruct.cast(value).getDefiniteValue();
		final ArrayItem[] items = array.items(arrayObject.getScope());
		final ArrayItem[] defItems = new ArrayItem[items.length];

		for (int i = 0; i < items.length; ++i) {

			final Ref valueRef = items[i].getValueRef();
			final Ref defValueRef = valueRef.rescope(rescoper);

			defItems[i] = new ArrayItem(i, defValueRef);
		}

		final Value<Array> result = sourceStruct.constantValue(
				new Array(
						array,
						array.distributeIn(ref.getContainer()),
						array.getValueStruct().rescope(rescoper),
						defItems));

		return result;
	}

	private final Ref ref;
	private final boolean toConstant;
	private ArrayValueStruct fromStruct;
	private ArrayValueStruct toStruct;

	ArrayCopyValueDef(Ref ref, boolean toConstant) {
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
		return this.toStruct = fromValueStruct().setConstant(this.toConstant);
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
		return arrayValue(this.ref, resolver, this.toConstant);
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

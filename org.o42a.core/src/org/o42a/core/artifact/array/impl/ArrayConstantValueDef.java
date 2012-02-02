/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class ArrayConstantValueDef extends ValueDef {

	private final Value<Array> value;
	private ArrayValueStruct valueStruct;

	public ArrayConstantValueDef(
			Obj source,
			LocationInfo location,
			ArrayValueStruct valueStruct,
			Array value) {
		super(
				source,
				location,
				noScopeUpgrade(valueStruct.toScoped().getScope()));
		this.value = valueStruct.compilerValue(value);
	}

	private ArrayConstantValueDef(
			ArrayConstantValueDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.value = prototype.value;
	}

	@Override
	public ArrayValueStruct getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ArrayValueStruct valueStruct =
				(ArrayValueStruct) this.value.getValueStruct();

		return this.valueStruct =
				valueStruct.prefixWith(getScopeUpgrade().toPrefix());
	}

	public final Array getArray() {
		return this.value.getCompilerValue();
	}

	@Override
	public void normalize(Normalizer normalizer) {
	}

	@Override
	protected boolean hasConstantValue() {

		final Array array = getArray();

		return array.isConstant() && array.hasStaticItems();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.value;
	}

	@Override
	protected ArrayConstantValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ArrayConstantValueDef(this, upgrade);
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected Logical buildLogical() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.value.resolveAll(resolver);
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		if (hasConstantValue()) {
			return this.value.op(dirs.getBuilder(), dirs.code());
		}

		final ArrayValueType valueType = (ArrayValueType) getValueType();
		final ArrayValueTypeIR valueTypeIR = valueType.ir(dirs.getGenerator());
		final ArrayIR arrayIR = getArray().ir(valueTypeIR);
		final ObjectOp array =
				getArray().getPrefix()
				.write(dirs.dirs(), host)
				.materialize(dirs.dirs());
		final ObjectValFunc constructor =
				arrayIR.getConstructor().op(arrayIR.getId(), dirs.code());

		return constructor.call(dirs, array);
	}

	@Override
	protected String name() {
		return "ArrayConstantValueDef";
	}

}

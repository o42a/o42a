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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.path.PrefixPath.emptyPrefix;

import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.array.ArrayValueStruct;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;


public class ArrayConstantValueDef extends ValueDef {

	private final Value<Array> value;
	private ArrayValueStruct valueStruct;

	public ArrayConstantValueDef(
			Obj source,
			LocationInfo location,
			ArrayValueStruct valueStruct,
			Array value) {
		super(source, location, emptyPrefix(valueStruct.toScoped().getScope()));
		this.value = valueStruct.compilerValue(value);
	}

	private ArrayConstantValueDef(
			ArrayConstantValueDef prototype,
			PrefixPath prefix) {
		super(prototype, prefix);
		this.value = prototype.value;
	}

	@Override
	public ArrayValueStruct getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final ArrayValueStruct valueStruct =
				(ArrayValueStruct) this.value.getValueStruct();

		return this.valueStruct = valueStruct.prefixWith(getPrefix());
	}

	@Override
	protected boolean hasConstantValue() {

		final Array array = this.value.getCompilerValue();

		return array.isConstant() && array.hasStaticItems();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.value;
	}

	@Override
	protected ArrayConstantValueDef create(
			PrefixPath prefix,
			PrefixPath additionalPrefix) {
		return new ArrayConstantValueDef(this, prefix);
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
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String name() {
		return "ArrayConstantValueDef";
	}

}

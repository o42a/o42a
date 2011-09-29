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
package org.o42a.core.artifact.array;

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.array.impl.ArrayValueType;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public class ArrayValueStruct
		extends ValueStruct<ArrayValueStruct, ArrayItem[]> {

	public static final ArrayItem[] NO_ITEMS = new ArrayItem[0];

	private final TypeRef itemTypeRef;

	public ArrayValueStruct(TypeRef itemTypeRef, boolean constant) {
		super(
				constant ? ValueType.CONST_ARRAY : ValueType.ARRAY,
				NO_ITEMS.getClass());
		this.itemTypeRef = itemTypeRef;
	}

	public final boolean isConstant() {
		return arrayValueType().isConstant();
	}

	public final TypeRef getItemTypeRef() {
		return this.itemTypeRef;
	}

	@Override
	public boolean assignableFrom(ValueStruct<?, ?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayValueStruct rescope(Rescoper rescoper) {
		return new ArrayValueStruct(
				this.itemTypeRef.rescope(rescoper),
				isConstant());
	}

	@Override
	public ValueDef valueDef(Ref ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CondDef condDef(Ref ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Value<ArrayItem[]> rescope(
			Value<ArrayItem[]> value,
			Rescoper rescoper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Value<ArrayItem[]> resolveAll(
			Value<ArrayItem[]> value,
			Resolver resolver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ValueStructIR<ArrayValueStruct, ArrayItem[]> createIR(
			Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

	private final ArrayValueType arrayValueType() {
		return (ArrayValueType) getValueType();
	}

}

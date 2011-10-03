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
import org.o42a.core.artifact.array.impl.ArrayCopyValueDef;
import org.o42a.core.artifact.array.impl.ArrayValueType;
import org.o42a.core.def.Rescoper;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.value.ValueStructIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;


public class ArrayValueStruct
		extends ValueStruct<ArrayValueStruct, Array> {

	private final TypeRef itemTypeRef;

	public ArrayValueStruct(TypeRef itemTypeRef, boolean constant) {
		super(
				constant ? ValueType.CONST_ARRAY : ValueType.ARRAY,
				Array.class);
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
	public ValueAdapter adapter(Ref ref, ValueStruct<?, ?> expectedStruct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueDef defaultValueDef(Ref ref) {
		return new ArrayCopyValueDef(ref, isConstant());
	}

	public ArrayValueStruct reproduce(Reproducer reproducer) {

		final TypeRef itemTypeRef = getItemTypeRef().reproduce(reproducer);

		if (itemTypeRef == null) {
			return null;
		}

		return new ArrayValueStruct(itemTypeRef, isConstant());
	}

	@Override
	protected Value<Array> resolveAll(
			Value<Array> value,
			Resolver resolver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ValueStructIR<ArrayValueStruct, Array> createIR(
			Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

	private final ArrayValueType arrayValueType() {
		return (ArrayValueType) getValueType();
	}

}

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
package org.o42a.core.value.array;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.value.array.ArrayValueTypeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


public class ArrayValueType extends ValueType<ArrayValueStruct> {

	public static final ArrayValueType ROW = new ArrayValueType(false);
	public static final ArrayValueType ARRAY = new ArrayValueType(true);

	private final boolean variable;
	private ArrayValueTypeIR ir;

	private ArrayValueType(boolean variable) {
		super(variable ? "array" : "row");
		this.variable = variable;
	}

	@Override
	public boolean isStateful() {
		return isVariable();
	}

	@Override
	public final boolean isVariable() {
		return this.variable;
	}

	public final TypeParameters typeParameters(TypeRef itemTypeRef) {
		return new TypeParameters(itemTypeRef, this).add(itemTypeRef);
	}

	public final ArrayValueStruct arrayStruct(TypeRef itemTypeRef) {
		return new ArrayValueStruct(this, itemTypeRef);
	}

	@Override
	public Obj typeObject(Intrinsics intrinsics) {
		if (isVariable()) {
			return intrinsics.getArray();
		}
		return intrinsics.getRow();
	}

	@Override
	public Path path(Intrinsics intrinsics) {

		final Obj array = typeObject(intrinsics);

		return Path.ROOT_PATH.append(array.getScope().toField().getKey());
	}

	@Override
	public final LinkValueType toLinkType() {
		return null;
	}

	@Override
	public final ArrayValueType toArrayType() {
		return this;
	}

	public final ArrayValueTypeIR ir(Generator generator) {
		if (this.ir != null && this.ir.getGenerator() == generator) {
			return this.ir;
		}
		return this.ir = new ArrayValueTypeIR(generator, this);
	}

}

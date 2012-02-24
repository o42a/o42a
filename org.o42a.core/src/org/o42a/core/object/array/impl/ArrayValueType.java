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
package org.o42a.core.object.array.impl;

import org.o42a.codegen.Generator;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Intrinsics;
import org.o42a.core.value.ValueType;


public class ArrayValueType extends ValueType<ArrayValueStruct> {

	private final boolean constant;
	private ArrayValueTypeIR ir;

	public ArrayValueType(boolean constant) {
		super(constant ? "const_array" : "array");
		this.constant = constant;
	}

	@Override
	public final boolean isVariable() {
		return !isConstant();
	}

	public final boolean isConstant() {
		return this.constant;
	}

	@Override
	public Path path(Intrinsics intrinsics) {

		final Obj array;

		if (!isConstant()) {
			array = intrinsics.getVariableArray();
		} else {
			array = intrinsics.getConstantArray();
		}

		return Path.ROOT_PATH.append(array.getScope().toField().getKey());
	}

	@Override
	public final LinkValueType toLinkType() {
		return null;
	}

	public final ArrayValueTypeIR ir(Generator generator) {
		if (this.ir != null && this.ir.getGenerator() == generator) {
			return this.ir;
		}
		return this.ir = new ArrayValueTypeIR(generator, this);
	}

}

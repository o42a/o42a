/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import static org.o42a.core.ir.IRNames.CONST_ID;
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Global;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;


public final class FalseValue<T> extends Value<T> {

	private static Ptr<ValType.Op> cachedPtr;
	private static Generator cachedGenerator;

	public FalseValue(TypeParameters<T> typeParameters) {
		super(typeParameters, ValueKnowledge.FALSE_VALUE);
	}

	@Override
	public T getCompilerValue() {
		return null;
	}

	@Override
	public Val val(Generator generator) {
		return FALSE_VAL;
	}

	@Override
	public Ptr<ValType.Op> valPtr(Generator generator) {
		if (cachedPtr != null && cachedGenerator == generator) {
			return cachedPtr;
		}
		cachedGenerator = generator;

		final Global<ValType.Op, ValType> global =
				generator.newGlobal().setConstant().dontExport().newInstance(
						CONST_ID.sub("FALSE"),
						VAL_TYPE,
						FALSE_VAL);

		return cachedPtr = global.getPointer();
	}

}

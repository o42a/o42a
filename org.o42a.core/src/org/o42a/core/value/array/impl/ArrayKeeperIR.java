/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.value.array.impl;

import static org.o42a.core.ir.field.FldKind.ARRAY_KEEPER;
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;
import static org.o42a.core.value.array.impl.ArrayKeeperIRType.ARRAY_KEEPER_IR_TYPE;

import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


public final class ArrayKeeperIR
		extends KeeperIR<ArrayKeeperIROp, ArrayKeeperIRType> {

	public ArrayKeeperIR(
			TypeParameters<Array> typeParameters,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		super(typeParameters, bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return ARRAY_KEEPER;
	}

	@Override
	public ArrayKeeperIRType getType() {
		return ARRAY_KEEPER_IR_TYPE;
	}

	@Override
	public void allocated(ArrayKeeperIRType instance) {
	}

	@Override
	public void fill(ArrayKeeperIRType instance) {

		final ValType val = instance.value();
		final Scope scope = getBodyIR().getObjectIR().getObject().getScope();
		final Value<?> value = getKeeper().getValue().value(scope.resolver());

		if (!value.getKnowledge().isInitiallyKnown()) {
			val.set(INDEFINITE_VAL);
		} else if (value.getKnowledge().isFalse()) {
			val.set(FALSE_VAL);
		} else {

			final ArrayValueType arrayType = getValueType().toArrayType();
			final Array array = arrayType.cast(value).getCompilerValue();

			val.set(arrayType.ir(getGenerator()).staticsIR().val(array));
		}
	}

}

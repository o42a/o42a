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
package org.o42a.core.value.floats;

import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.value.floats.FloatKeeperIRType.FLOAT_KEEPER_IR_TYPE;

import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


final class FloatKeeperIR
		extends KeeperIR<FloatKeeperIROp, FloatKeeperIRType> {

	FloatKeeperIR(
			TypeParameters<Double> typeParameters,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		super(typeParameters, bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return FldKind.INTEGER_KEEPER;
	}

	@Override
	public FloatKeeperIRType getType() {
		return FLOAT_KEEPER_IR_TYPE;
	}

	@Override
	public void allocated(FloatKeeperIRType instance) {
	}

	@Override
	public void fill(FloatKeeperIRType instance) {

		final Scope scope = getBodyIR().getObjectIR().getObject().getScope();
		final Value<?> value = getKeeper().getValue().value(scope.resolver());

		if (!value.getKnowledge().isKnownToCompiler()) {
			instance.flags().setValue((byte) VAL_INDEFINITE);
			instance.value().setValue(0);
		} else if (value.getKnowledge().isFalse()) {
			instance.flags().setValue((byte) 0);
			instance.value().setValue(0);
		} else {
			instance.flags().setValue((byte) VAL_CONDITION);
			instance.value().setValue(
					ValueType.FLOAT.cast(value).getCompilerValue());
		}
	}

}

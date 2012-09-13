/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.value.integer;

import static org.o42a.core.value.integer.IntegerKeeperIRType.INTEGER_KEEPER_IR_TYPE;

import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.object.state.Keeper;


final class IntegerKeeperIR
		extends KeeperIR<IntegerKeeperIROp, IntegerKeeperIRType> {

	IntegerKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		super(bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return FldKind.INTEGER_KEEPER;
	}

	@Override
	protected IntegerKeeperIRType allocateKeeper(ObjectIRBodyData data) {
		return data.getData().addInstance(
				getKeeper().toID(),
				INTEGER_KEEPER_IR_TYPE);
	}

}

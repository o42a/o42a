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
package org.o42a.core.value.voids;

import static org.o42a.core.ir.field.FldKind.VOID_KEEPER;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.value.voids.VoidKeeperIRType.VOID_KEEPER_IR_TYPE;

import org.o42a.codegen.data.Content;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.struct.SingleValueStructIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.Value;


public final class VoidKeeperIR
		extends KeeperIR<VoidKeeperIROp, VoidKeeperIRType>
		implements Content<VoidKeeperIRType> {

	public VoidKeeperIR(
			SingleValueStructIR<?> valueStructIR,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		super(valueStructIR, bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return VOID_KEEPER;
	}

	@Override
	public void allocated(VoidKeeperIRType instance) {
	}

	@Override
	public void fill(VoidKeeperIRType instance) {

		final Scope scope = getBodyIR().getObjectIR().getObject().getScope();
		final Value<?> value = getKeeper().getValue().value(scope.resolver());

		if (!value.getKnowledge().isKnownToCompiler()) {
			instance.flags().setValue((byte) VAL_INDEFINITE);
		} else if (value.getKnowledge().isFalse()) {
			instance.flags().setValue((byte) 0);
		} else {
			instance.flags().setValue((byte) VAL_CONDITION);
		}
	}

	@Override
	protected VoidKeeperIRType allocateKeeper(ObjectIRBodyData data) {
		return data.getData().addInstance(getId(), VOID_KEEPER_IR_TYPE, this);
	}

}
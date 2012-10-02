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
package org.o42a.core.value.link.impl;

import static org.o42a.core.value.link.impl.LinkKeeperIRType.LINK_KEEPER_IR_TYPE;

import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.DataRec;
import org.o42a.core.Scope;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.Value;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueStruct;


final class LinkKeeperIR
		extends KeeperIR<LinkKeeperIROp, LinkKeeperIRType>
		implements Content<LinkKeeperIRType> {

	LinkKeeperIR(
			AbstractLinkValueStructIR valueStructIR,
			ObjectIRBody bodyIR,
			Keeper keeper) {
		super(valueStructIR, bodyIR, keeper);
	}

	@Override
	public FldKind getKind() {
		return FldKind.LINK_KEEPER;
	}

	@Override
	public void allocated(LinkKeeperIRType instance) {
	}

	@Override
	public void fill(LinkKeeperIRType instance) {

		final DataRec object = instance.object();
		final Scope scope = getBodyIR().getObjectIR().getObject().getScope();
		final Value<?> value = getKeeper().getValue().value(scope.resolver());

		if (!value.getKnowledge().isInitiallyKnown()) {
			object.setNull();
		} else if (value.getKnowledge().isFalse()) {

			final Obj none = getBodyIR().getAscendant().getContext().getNone();

			object.setValue(
					none.ir(getGenerator())
					.getMainBodyIR()
					.pointer(getGenerator())
					.toData());
		} else {

			final LinkValueStruct linkStruct =
					getValueStructIR().getValueStruct().toLinkStruct();
			final KnownLink link = linkStruct.cast(value).getCompilerValue();

			object.setValue(
					link.getTarget()
					.ir(getGenerator())
					.getMainBodyIR()
					.pointer(getGenerator())
					.toData());
		}
	}

	@Override
	protected LinkKeeperIRType allocateKeeper(ObjectIRBodyData data) {
		return data.getData().addInstance(getId(), LINK_KEEPER_IR_TYPE, this);
	}

}

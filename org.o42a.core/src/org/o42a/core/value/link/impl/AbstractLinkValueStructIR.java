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

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyOp;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.struct.AbstractValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.util.string.ID;


abstract class AbstractLinkValueStructIR
		extends AbstractValueStructIR<LinkValueStruct, KnownLink> {

	private int constSeq;

	AbstractLinkValueStructIR(
			Generator generator,
			LinkValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public Val val(KnownLink value) {

		final Obj target =
				value.getTargetRef()
				.getRef()
				.getResolution()
				.toObject();
		final ObjectIR targetIR = target.ir(getGenerator());
		final Ptr<ObjectIRBodyOp> mainBodyPtr =
				targetIR.getMainBodyIR().pointer(getGenerator());

		return new Val(
				getValueStruct(),
				Val.VAL_CONDITION,
				0,
				mainBodyPtr.toAny());
	}

	@Override
	public KeeperIR<?, ?> createKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		return new LinkKeeperIR(this, bodyIR, keeper);
	}

	@Override
	protected ID constId(KnownLink value) {
		return constIdPrefix().anonymous(++this.constSeq);
	}

	protected abstract ID constIdPrefix();

}

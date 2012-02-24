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
package org.o42a.core.object.link.impl;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.struct.AbstractValueStructIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.ObjectLink;


public class LinkValueStructIR
		extends AbstractValueStructIR<LinkValueStruct, ObjectLink> {

	private int constSeq;

	public LinkValueStructIR(Generator generator, LinkValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public Val val(ObjectLink value) {

		final Obj target =
				value.getTargetRef().getRef().getResolution().materialize();
		final ObjectIR targetIR = target.ir(getGenerator());
		final Ptr<ObjectBodyIR.Op> mainBodyPtr =
				targetIR.getMainBodyIR().pointer(getGenerator());

		return new Val(
				getValueStruct(),
				Val.CONDITION_FLAG,
				0,
				mainBodyPtr.toAny());
	}

	@Override
	protected CodeId constId(ObjectLink value) {
		return getGenerator().id("CONST").sub("LINK")
				.anonymous(++this.constSeq);
	}

}

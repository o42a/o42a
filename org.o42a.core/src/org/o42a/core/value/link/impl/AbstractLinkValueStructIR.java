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
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.state.KeeperIR;
import org.o42a.core.ir.value.struct.AbstractValueStructIR;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueStruct;


abstract class AbstractLinkValueStructIR
		extends AbstractValueStructIR<LinkValueStruct, KnownLink> {

	AbstractLinkValueStructIR(
			Generator generator,
			LinkValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public KeeperIR<?, ?> createKeeperIR(ObjectIRBody bodyIR, Keeper keeper) {
		return new LinkKeeperIR(this, bodyIR, keeper);
	}

}

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.*;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.ir.value.struct.ValueStructIR;
import org.o42a.core.object.link.KnownLink;
import org.o42a.core.object.link.LinkValueStruct;


public class GetterValueStructIR
		extends ValueStructIR<LinkValueStruct, KnownLink> {

	public GetterValueStructIR(
			Generator generator,
			LinkValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public Ptr<Op> valPtr(KnownLink value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Val val(KnownLink value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return new GetterIR(this, objectIR);
	}

	@Override
	public ValHolder tempValHolder(ValOp value) {
		return new LinkValHolder(value, true);
	}

	@Override
	public ValHolder volatileValHolder(ValOp value) {
		return new LinkValHolder(value, true);
	}

	@Override
	public ValHolder valTrap(ValOp value) {
		return new LinkValTrap(value);
	}

}

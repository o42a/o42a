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

import static org.o42a.core.ir.IRNames.CONST_ID;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueIR;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.util.string.ID;


public class VariableValueStructIR extends AbstractLinkValueStructIR {

	private static final ID VAR_CONST_ID = CONST_ID.sub("VAR");

	public VariableValueStructIR(
			Generator generator,
			LinkValueStruct valueStruct) {
		super(generator, valueStruct);
	}

	@Override
	public ValueIR valueIR(ObjectIR objectIR) {
		return new VariableIR(this, objectIR);
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

	@Override
	protected ID constIdPrefix() {
		return VAR_CONST_ID;
	}

}

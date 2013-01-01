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
package org.o42a.core.value.string;

import static org.o42a.core.ir.field.FldKind.STRING_KEEPER;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


final class StringKeeperIRType extends KeeperIRType<StringKeeperIROp> {

	static final StringKeeperIRType STRING_KEEPER_IR_TYPE =
			new StringKeeperIRType();

	private ValType value;

	private StringKeeperIRType() {
		super(ID.id("o42a_kpr_string"));
	}

	public final ValType value() {
		return this.value;
	}

	@Override
	public StringKeeperIROp op(StructWriter<StringKeeperIROp> writer) {
		return new StringKeeperIROp(writer);
	}

	@Override
	protected void allocate(SubData<StringKeeperIROp> data) {
		this.value = data.addInstance(ID.id("value"), VAL_TYPE);
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0200 | STRING_KEEPER.code());
	}

}

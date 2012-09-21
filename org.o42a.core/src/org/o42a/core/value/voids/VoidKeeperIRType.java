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

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.util.string.ID;


final class VoidKeeperIRType extends KeeperIRType<VoidKeeperIROp> {

	public static final VoidKeeperIRType VOID_KEEPER_IR_TYPE =
			new VoidKeeperIRType();

	private Int8rec flags;

	private VoidKeeperIRType() {
		super(ID.id("o42a_kpr_void"));
	}

	public final Int8rec flags() {
		return this.flags;
	}

	@Override
	public VoidKeeperIROp op(StructWriter<VoidKeeperIROp> writer) {
		return new VoidKeeperIROp(writer);
	}

	@Override
	protected void allocate(SubData<VoidKeeperIROp> data) {
		this.flags = data.addInt8("flags");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0200 | VOID_KEEPER.code());
	}

}
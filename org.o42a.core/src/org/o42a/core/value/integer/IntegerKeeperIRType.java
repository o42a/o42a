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
package org.o42a.core.value.integer;

import static org.o42a.core.ir.field.FldKind.INTEGER_KEEPER;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Int64rec;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.util.string.ID;


final class IntegerKeeperIRType extends KeeperIRType<IntegerKeeperIROp> {

	public static final IntegerKeeperIRType INTEGER_KEEPER_IR_TYPE =
			new IntegerKeeperIRType();

	private Int64rec value;
	private Int8rec flags;

	private IntegerKeeperIRType() {
		super(ID.id("o42a_kpr_integer"));
	}

	public final Int64rec value() {
		return this.value;
	}

	public final Int8rec flags() {
		return this.flags;
	}

	@Override
	public IntegerKeeperIROp op(StructWriter<IntegerKeeperIROp> writer) {
		return new IntegerKeeperIROp(writer);
	}

	@Override
	protected void allocate(SubData<IntegerKeeperIROp> data) {
		this.value = data.addInt64("value");
		this.flags = data.addInt8("flags");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0200 | INTEGER_KEEPER.code());
	}

}

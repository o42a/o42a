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
package org.o42a.core.value.floats;

import static org.o42a.core.ir.field.FldKind.FLOAT_KEEPER;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Fp64rec;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.util.string.ID;


final class FloatKeeperIRType extends KeeperIRType<FloatKeeperIROp> {

	public static final FloatKeeperIRType FLOAT_KEEPER_IR_TYPE =
			new FloatKeeperIRType();

	private Fp64rec value;
	private Int8rec flags;

	private FloatKeeperIRType() {
		super(ID.id("o42a_kpr_float"));
	}

	public final Fp64rec value() {
		return this.value;
	}

	public final Int8rec flags() {
		return this.flags;
	}

	@Override
	public FloatKeeperIROp op(StructWriter<FloatKeeperIROp> writer) {
		return new FloatKeeperIROp(writer);
	}

	@Override
	protected void allocate(SubData<FloatKeeperIROp> data) {
		this.value = data.addFp64("value");
		this.flags = data.addInt8("flags");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0200 | FLOAT_KEEPER.code());
	}

}

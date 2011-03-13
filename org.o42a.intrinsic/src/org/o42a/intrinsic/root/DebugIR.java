/*
    Intrinsics
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.core.ir.object.RuntimeObjectType.RUNTIME_OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;


class DebugIR extends Struct<DebugIR.Op> {

	private AnyPtrRec rtypeTypeInfo;

	DebugIR() {
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.rawId("o42a_dbg_data");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		RUNTIME_OBJECT_TYPE.data(getGenerator());
		this.rtypeTypeInfo = data.addPtr("rtype_type_info");
	}

	@Override
	protected void fill() {
		this.rtypeTypeInfo.setValue(
				RUNTIME_OBJECT_TYPE.getTypeInfo().pointer(getGenerator())
				.toAny());
	}

	static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}

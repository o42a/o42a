/*
    Compiler Core
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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectDataType.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.StructPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class RuntimeObjectType extends Type<RuntimeObjectType.Op> {

	public static final RuntimeObjectType RUNTIME_OBJECT_TYPE =
		new RuntimeObjectType();

	private ObjectDataType data;
	private StructPtrRec<ObjectType.Op> sample;

	private RuntimeObjectType() {
	}

	public final ObjectDataType data() {
		return this.data;
	}

	public final StructPtrRec<ObjectType.Op> sample() {
		return this.sample;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("RuntimeObjectType");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.data = data.addInstance(
				getGenerator().id("data"),
				OBJECT_DATA_TYPE);
		this.sample = data.addPtr("sample", OBJECT_TYPE);
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final RuntimeObjectType getType() {
			return (RuntimeObjectType) super.getType();
		}

		public final ObjectDataType.Op data(Code code) {
			return writer().struct(code, getType().data());
		}

	}

}

/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class RuntimeObjectType extends Type<RuntimeObjectType.Op> {

	public static final RuntimeObjectType RUNTIME_OBJECT_TYPE =
			new RuntimeObjectType();

	private static final Type<?>[] TYPE_DEPENDENCIES =
			new Type<?>[] {OBJECT_TYPE};

	private ObjectIRData data;
	private StructRec<ObjectIRType.Op> sample;

	private RuntimeObjectType() {
	}

	@Override
	public final Type<?>[] getTypeDependencies() {
		return TYPE_DEPENDENCIES;
	}

	public final ObjectIRData data() {
		return this.data;
	}

	public final StructRec<ObjectIRType.Op> sample() {
		return this.sample;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
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

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final RuntimeObjectType getType() {
			return (RuntimeObjectType) super.getType();
		}

		public final ObjectIRData.Op data(Code code) {
			return struct(null, code, getType().data());
		}

	}

}

/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.debug;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


final class DbgStackFrameType extends Type<DbgStackFrameType.Op> {

	private AnyPtrRec name;
	private AnyPtrRec prev;

	DbgStackFrameType() {
		super("DEBUG.StackFrame");
	}

	public final AnyPtrRec getName() {
		return this.name;
	}

	public final AnyPtrRec getPrev() {
		return this.prev;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.name = data.addPtr("name");
		this.prev = data.addPtr("prev");
	}

	public static class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final DbgStackFrameType getType() {
			return (DbgStackFrameType) super.getType();
		}

		public final DataOp<AnyOp> name(Code code) {
			return writer().ptr(code, getType().getName());
		}

		public final DataOp<AnyOp> prev(Code code) {
			return writer().ptr(code, getType().getPrev());
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

}

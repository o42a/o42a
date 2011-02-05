/*
    Compiler Core
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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.RelPtrRec;
import org.o42a.codegen.data.SubData;


public final class SampleDescIR implements Content<SampleDescIR.Type> {

	private final ObjectBodyIR bodyIR;

	SampleDescIR(ObjectBodyIR bodyIR) {
		this.bodyIR = bodyIR;
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {
		instance.getBody().setValue(
				this.bodyIR.getData().getPointer().relativeTo(
						instance.getPointer()));
	}

	@Override
	public String toString() {
		return this.bodyIR.toString();
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<RelOp> body(Code code) {
			return writer().relPtr(code, getType().getBody());
		}

		@Override
		public final Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

	public static final class Type
			extends org.o42a.codegen.data.Type<SampleDescIR.Op> {

		private RelPtrRec body;

		Type(ObjectIRGenerator generator) {
			super(generator.id("SampleDesc"));
		}

		public final RelPtrRec getBody() {
			return this.body;
		}

		@Override
		public final Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.body = data.addRelPtr("body");
		}

	}

}

/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.inst;

import static org.o42a.util.string.ID.rawId;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyRec;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;


public final class ResumeFromFld
		extends InstFld<ResumeFromFld.Op, ResumeFromFld.Type>
		implements Content<ResumeFromFld.Type> {

	public static final Type RESUME_FROM_FLD = new Type();

	ResumeFromFld(ObjectIRBody bodyIR) {
		super(bodyIR);
	}

	@Override
	public InstFldKind getInstFldKind() {
		return InstFldKind.INST_RESUME_FROM;
	}

	@Override
	public final ResumeFromOp op(Code code, ObjOp host) {
		return new ResumeFromOp(
				host,
				this,
				code.means(c -> host.ptr().field(c, getTypeInstance())));
	}

	@Override
	public InstFld<Op, Type> derive(ObjectIRBody inheritantBodyIR) {
		return new ResumeFromFld(inheritantBodyIR);
	}

	@Override
	public void fill(Type instance) {
		instance.resumePtr().setNull();
	}

	@Override
	protected Type getType() {
		return RESUME_FROM_FLD;
	}

	@Override
	protected Content<Type> content() {
		return this;
	}

	public static final class Op extends StructOp<Op> {

		Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final AnyRecOp resumePtr(Code code) {
			return ptr(null, code, getType().resumePtr());
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private AnyRec resumePtr;

		Type() {
			super(rawId("o42a_fld_resume_from"));
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		public final AnyRec resumePtr() {
			return this.resumePtr;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.resumePtr = data.addPtr("resume_ptr");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.RESUME_FROM.code());
		}

	}

}

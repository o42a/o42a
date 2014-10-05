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
package org.o42a.core.ir.field.local;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjectIRBodies;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class LocalIR implements FldIR<LocalIR.Op, LocalIR.Type> {

	public static final Type LOCAL_IR_TYPE = new Type();

	public static final ID LOCAL_ID = ID.id("local");

	private final ObjectIRBody bodyIR;
	private final MemberLocal member;
	private Type instance;

	public LocalIR(ObjectIRBody bodyIR, MemberLocal member) {
		this.bodyIR = bodyIR;
		this.member = member;
	}

	@Override
	public final ID getId() {
		return getMember().getId();
	}

	@Override
	public final FldKind getKind() {
		return FldKind.LOCAL;
	}

	public final MemberLocal getMember() {
		return this.member;
	}

	public final MemberKey getKey() {
		return getMember().getMemberKey();
	}

	@Override
	public final Obj getDeclaredIn() {
		return getKey().getOrigin().toObject();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public Type getInstance() {
		if (this.instance == null) {
			getBodyIR().bodies().getStruct().allocate();
			assert this.instance != null :
				this + " not allocated";
		}
		return this.instance;
	}

	@Override
	public FldIR<Op, Type> get(ObjectIRBodies bodies) {
		return bodies.local(getKey());
	}

	public final LocalIROp op(Code code, ObjectOp host) {
		return new LocalIROp(
				host,
				code.means(c -> host.ptr().field(c, getTypeInstance())));
	}

	public final void allocate(SubData<?> data) {
		this.instance = data.addNewInstance(
				LOCAL_ID.detail(getId()),
				LOCAL_IR_TYPE,
				instance -> instance.object().setNull());
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp object(ID id, Code code) {
			return ptr(id, code, getType().object());
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec object;

		private Type() {
			super(ID.rawId("o42a_fld_local"));
		}

		public final DataRec object() {
			return this.object;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.LOCAL.code());
		}

	}

}

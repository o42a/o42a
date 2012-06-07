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
package org.o42a.core.ir.field.getter;

import static org.o42a.core.ir.object.op.ObjectRefFunc.OBJECT_REF;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.RefFld;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.ir.object.op.ObjectRefFunc;
import org.o42a.core.ir.object.op.ObjectRefFunc.ObjectRef;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;


public class GetterFld extends RefFld<ObjectRefFunc> {

	public static final Type GETTER_FLD = new Type();

	public GetterFld(ObjectBodyIR bodyIR, Field field, Obj target) {
		super(bodyIR, field, target);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.GETTER;
	}

	@Override
	public Type getInstance() {
		return (Type) super.getInstance();
	}

	@Override
	public GetterFldOp op(Code code, ObjOp host) {
		return new GetterFldOp(
				this,
				host,
				isOmitted() ? null : host.ptr().field(code, getInstance()));
	}

	@Override
	protected Type getType() {
		return GETTER_FLD;
	}

	@Override
	protected boolean mayOmit() {
		return false;
	}

	public static final class Op extends RefFld.Op<Op, ObjectRefFunc> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		@Override
		protected DataOp construct(
				Code code,
				ObjOp host,
				ObjectRefFunc constructor) {
			return constructor.call(code, host);
		}

	}

	public static final class Type extends RefFld.Type<Op, ObjectRefFunc> {

		private Type() {
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.rawId("o42a_fld_getter");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0200 | FldKind.GETTER.code());
		}

		@Override
		protected ObjectRef getSignature() {
			return OBJECT_REF;
		}

		@Override
		protected FuncPtr<ObjectRefFunc> constructorStub() {
			throw new IllegalStateException(
					"The getter constructor can not be a stub");
		}

	}
}

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
package org.o42a.core.ir.field.scope;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.field.Field;


public final class ScopeFld extends Fld implements Content<ScopeFld.Type> {

	public static final Type SCOPE_FLD = new Type();

	private ObjectBodyIR target;

	public ScopeFld(ObjectBodyIR bodyIR, Field<Obj> field) {
		super(bodyIR, field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.SCOPE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Field<Obj> getField() {
		return (Field<Obj>) super.getField();
	}

	@Override
	public boolean isOverrider() {
		return false;
	}

	@Override
	public final Type getInstance() {
		return (Type) super.getInstance();
	}

	public final ObjectBodyIR getTarget() {
		return this.target;
	}

	@Override
	public final ScopeFldOp op(Code code, ObjOp host) {
		return new ScopeFldOp(
				this,
				host,
				isOmitted() ? null : host.ptr().field(code, getInstance()));
	}

	public final void declare(SubData<?> data, ObjectBodyIR target) {
		this.target = target;
		allocate(data);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {
		instance.object().setConstant(true).setValue(
				this.target.pointer(instance.getGenerator()).toData());
	}

	@Override
	protected Type getType() {
		return SCOPE_FLD;
	}

	@Override
	protected Content<?> content() {
		return this;
	}

	public static final class Op extends Fld.Op<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp object(Code code) {
			return ptr(null, code, getType().object());
		}

	}

	public static final class Type extends Fld.Type<Op> {

		private DataRec object;

		private Type() {
		}

		public final DataRec object() {
			return this.object;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("ScopeFld");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
		}

	}

}

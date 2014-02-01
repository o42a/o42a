/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.object.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class ScopeFld
		extends MemberFld<ScopeFld.Op>
		implements Content<ScopeFld.Type> {

	public static final Type SCOPE_FLD = new Type();

	private Obj ascendant;
	private ObjectIRBody target;

	public ScopeFld(Field field) {
		super(field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.SCOPE;
	}

	@Override
	public boolean isOverrider() {
		return false;
	}

	@Override
	public final Type getInstance() {
		return (Type) super.getInstance();
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	public final ObjectIRBody getTarget() {
		return this.target;
	}

	public final void declare(ObjectIRBodyData data, Obj target) {
		this.ascendant = target;
		if (!target.getConstructionMode().isRuntime()) {
			this.target = target.ir(data.getGenerator()).getMainBodyIR();
		} else {
			this.target = null;
		}
		allocate(data);
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final DataRec objectRec = instance.object().setConstant(true);
		final ObjectIRBody target = getTarget();

		if (target != null) {
			objectRec.setValue(
					target.pointer(instance.getGenerator()).toData());
		} else {
			objectRec.setNull();
		}
	}

	@Override
	protected Type getType() {
		return SCOPE_FLD;
	}

	@Override
	protected boolean mayOmit() {
		if (super.mayOmit()) {
			return true;
		}

		final ObjectIR scopeIR =
				getField().getFirstDeclaration().toObject().ir(getGenerator());

		return scopeIR.isExact();
	}

	@Override
	protected Content<ScopeFld.Type> content() {
		return this;
	}

	@Override
	protected MemberFldOp<Op> op(Code code, ObjOp host, Op ptr) {
		return new ScopeFldOp(this, host, ptr);
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
			super(ID.rawId("o42a_fld_scope"));
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
			return externalTypeInfo(0x042a0200 | FldKind.SCOPE.code());
		}

	}

}

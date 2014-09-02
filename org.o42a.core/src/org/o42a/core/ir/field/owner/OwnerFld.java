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
package org.o42a.core.ir.field.owner;

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


public final class OwnerFld
		extends MemberFld<OwnerFld.Op>
		implements Content<OwnerFld.Type> {

	public static final Type SCOPE_FLD = new Type();

	private Obj ascendant;
	private ObjectIR targetIR;
	private boolean dummy;

	public OwnerFld(Field field) {
		super(field);
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OWNER;
	}

	@Override
	public boolean isDummy() {
		return this.dummy;
	}

	@Override
	public final Type getInstance() {
		return (Type) super.getInstance();
	}

	public final Obj getAscendant() {
		return this.ascendant;
	}

	public final ObjectIR getTargetIR() {
		return this.targetIR;
	}

	public final void declare(ObjectIRBodyData data, Obj target) {
		this.ascendant = target;
		if (!target.getConstructionMode().isRuntime()) {
			this.targetIR = target.ir(data.getGenerator());
		} else {
			this.targetIR = null;
		}
		allocate(data);
	}

	public final void declareDummy(ObjectIRBodyData data) {
		this.dummy = true;
		allocate(data);
	}

	@Override
	public void allocateMethods(SubData<VmtIROp> vmt) {
	}

	@Override
	public void fillMethods() {
	}

	@Override
	public void allocated(Type instance) {
	}

	@Override
	public void fill(Type instance) {

		final DataRec objectRec = instance.object().setConstant(true);
		final ObjectIR targetIR = getTargetIR();

		if (targetIR != null) {
			objectRec.setValue(targetIR.ptr().toData());
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
	protected Content<OwnerFld.Type> content() {
		return this;
	}

	@Override
	protected Content<OwnerFld.Type> dummyContent() {
		return this;
	}

	@Override
	protected MemberFldOp<Op> op(Code code, ObjOp host, Op ptr) {
		assert !isDummy() :
			"Dummy owner field accessd: " + this;
		return new OwnerFldOp(this, host, ptr);
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
			super(ID.rawId("o42a_fld_owner"));
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
			return externalTypeInfo(0x042a0200 | FldKind.OWNER.code());
		}

	}

}

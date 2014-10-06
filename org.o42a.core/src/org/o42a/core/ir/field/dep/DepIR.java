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
package org.o42a.core.ir.field.dep;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.field.local.LocalIR;
import org.o42a.core.ir.object.ObjectIRBodies;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class DepIR implements FldIR<DepIR.Op, DepIR.Type> {

	public static final Type DEP_IR = new Type();

	private final ObjectIRBody bodyIR;
	private final Dep dep;
	private final RefIR refIR;
	private Type instance;

	public DepIR(ObjectIRBody bodyIR, Dep dep) {
		assert dep.exists(bodyIR.getGenerator().getAnalyzer()) :
			dep + " does not exist";
		this.bodyIR = bodyIR;
		this.dep = dep;
		this.refIR = dep.ref().ir(getGenerator());
	}

	public final Dep getDep() {
		return this.dep;
	}

	@Override
	public final ID getId() {
		return getDep().toID();
	}

	public final boolean isOmitted() {
		return this.refIR.isOmitted();
	}

	@Override
	public final boolean isStateless() {
		return isOmitted();
	}

	@Override
	public final FldKind getKind() {
		return FldKind.DEP;
	}

	@Override
	public final Obj getDeclaredIn() {
		return getDep().getDeclaredIn();
	}

	@Override
	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public final Type getInstance() {
		assert !isOmitted() :
			this + " is omitted";
		if (this.instance == null) {
			getBodyIR().bodies().getStruct().allocate();
		}
		return this.instance;
	}

	@Override
	public final DepIR get(ObjectIRBodies bodies) {
		return bodies.dep(getDep());
	}

	@Override
	public final void allocate(SubData<?> data) {
		if (isOmitted()) {
			return;
		}
		this.instance = data.addNewInstance(
				getId(),
				DEP_IR,
				instance -> instance.object().setNull());
	}

	public final DepOp op(Code code, ObjectOp host) {
		return new DepOp(code, host, this);
	}

	@Override
	public final Fld<?, ?> toFld() {
		return null;
	}

	@Override
	public final LocalIR toLocal() {
		return null;
	}

	@Override
	public String toString() {
		if (this.dep == null) {
			return super.toString();
		}
		return this.dep.toString();
	}

	final RefIR refIR() {
		return this.refIR;
	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec object;

		private Type() {
			super(ID.rawId("o42a_fld_dep"));
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
			return externalTypeInfo(0x042a0200 | FldKind.DEP.code());
		}

	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp object(Code code) {
			return ptr(null, code, getType().object());
		}

	}

}

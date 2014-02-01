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
package org.o42a.core.ir.object.dep;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.object.ObjectIRBodyData;
import org.o42a.core.ir.op.RefIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class DepIR implements FldIR {

	public static final Type DEP_IR = new Type();

	private final ObjectIRBody bodyIR;
	private final Dep dep;
	private final RefIR refIR;
	private Data<?> data;

	public DepIR(ObjectIRBody bodyIR, Dep dep) {
		assert dep.exists(bodyIR.getGenerator().getAnalyzer()) :
			dep + " does not exist";
		this.bodyIR = bodyIR;
		this.dep = dep;
		this.refIR = dep.ref().ir(getGenerator());
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	public final Dep getDep() {
		return this.dep;
	}

	@Override
	public final ID getId() {
		return getDep().toID();
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
	public final Data<?> data(Generator generator) {
		return this.data;
	}

	public final void allocate(ObjectIRBodyData data) {
		this.data = this.refIR.allocate(getId(), data.getData());
	}

	public final DepOp op(Code code, ObjOp host) {
		return new DepOp(code, host, this);
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

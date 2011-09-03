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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.member.local.Dep;


public class DepIR {

	public static final Type DEP_IR = new Type();

	private final Generator generator;
	private final Dep dep;
	private Type instance;

	public DepIR(Generator generator, Dep dep) {
		this.generator = generator;
		this.dep = dep;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final Dep getDep() {
		return this.dep;
	}

	public Type getInstance() {
		return this.instance;
	}

	public final DepOp op(Code code, ObjOp host) {
		assert getInstance() != null :
			this + " is not allocated yet";
		return new DepOp(
				this,
				host,
				host.ptr().dep(code, getInstance()));
	}

	@Override
	public String toString() {
		return this.dep + " IR";
	}

	void allocate(SubData<?> data) {

		final CodeId localId = localId();

		this.instance = data.addInstance(localId, DEP_IR);
		this.instance.object().setNull();
	}

	private CodeId localId() {
		switch (getDep().getKind()) {
		case FIELD_DEP:

			final FieldIR<?> depFieldIR =
					getDep().getDepField().ir(getGenerator());

			return getGenerator().id("F").sub(depFieldIR.getId().getLocal());
		case ENCLOSING_OWNER_DEP:
			return getGenerator().id("O");
		case REF_DEP:
			return getGenerator().id("R").anonymous(getDep().getName());
		}

		throw new IllegalStateException(
				"Dependency of unsupported kind: " + getDep());
	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

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
			return factory.id("Dep");
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
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

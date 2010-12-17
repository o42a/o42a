/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.core.ir.IRSymbolSeparator.DETAIL;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.StructPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;


public final class ObjectMethodsIR extends Struct<ObjectMethodsIR.Op> {

	private final ObjectBodyIR bodyIR;
	private StructPtrRec<ObjectType.Op> objectType;

	ObjectMethodsIR(ObjectBodyIR bodyIR) {
		super(id(bodyIR));
		this.bodyIR = bodyIR;
	}

	private static String id(ObjectBodyIR bodyIR) {

		final ObjectIR objectIR = bodyIR.getObjectIR();

		if (bodyIR.isMain()) {
			return objectIR.getId() + DETAIL + "type_methods";
		}

		final Obj ascendant = bodyIR.getAscendant();

		return (objectIR.getId() + DETAIL + "methods" + DETAIL
				+ ascendant.ir(objectIR.getGenerator()).getId());
	}

	public final IRGenerator getGenerator() {
		return this.bodyIR.getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.bodyIR.getObjectIR();
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public final Op op(StructWriter writer) {
		return new Op(writer, this);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectType =
			data.addPtr("object_type", getGenerator().objectType());
	}

	@Override
	protected void fill() {

		final ObjectIR ascendantIR =
			getBodyIR().getAscendant().ir(getGenerator());

		this.objectType.setValue(
				ascendantIR.getTypeIR().getObjectType().getPointer());
	}

	public static final class Op extends StructOp {

		private final ObjectMethodsIR methodsIR;

		private Op(StructWriter writer, ObjectMethodsIR metaIR) {
			super(writer);
			this.methodsIR = metaIR;
		}

		public final ObjectMethodsIR getMethodsIR() {
			return this.methodsIR;
		}

		public final DataOp<ObjectType.Op> objectType(Code code) {
			return writer().ptr(code, this.methodsIR.objectType);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer, this.methodsIR);
		}

	}

}

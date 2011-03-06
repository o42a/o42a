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

import static org.o42a.core.ir.object.ObjectType.OBJECT_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.StructPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;


public final class ObjectMethodsIR extends Struct<ObjectMethodsIR.Op> {

	private final ObjectBodyIR bodyIR;
	private StructPtrRec<ObjectType.Op> objectType;

	ObjectMethodsIR(ObjectBodyIR bodyIR) {
		this.bodyIR = bodyIR;
	}

	@Override
	public final Generator getGenerator() {
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
	protected CodeId buildCodeId(CodeIdFactory factory) {

		final ObjectIR objectIR = this.bodyIR.getObjectIR();
		final CodeId localId;

		if (this.bodyIR.isMain()) {
			localId = factory.id().detail("type_methods");
		} else {

			final Obj ascendant = this.bodyIR.getAscendant();

			localId = factory.id().detail("methods").detail(
					ascendant.ir(objectIR.getGenerator()).getId());
		}

		return objectIR.getId().setLocal(localId);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectType = data.addPtr("object_type", OBJECT_TYPE);
	}

	@Override
	protected void fill() {

		final ObjectIR ascendantIR =
			getBodyIR().getAscendant().ir(getGenerator());

		this.objectType.setValue(
				ascendantIR.getTypeIR().getObjectType()
				.data(getGenerator()).getPointer());
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

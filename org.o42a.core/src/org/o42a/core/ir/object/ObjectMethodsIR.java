/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public final class ObjectMethodsIR extends Struct<ObjectMethodsIR.Op> {

	static final ID METHODS_ID = ID.id().detail("methods");

	private final ObjectBodyIR bodyIR;
	private StructRec<ObjectIRType.Op> objectType;

	ObjectMethodsIR(ObjectBodyIR bodyIR) {
		super(buildId(bodyIR));
		this.bodyIR = bodyIR;
	}

	public final ObjectIR getObjectIR() {
		return this.bodyIR.getObjectIR();
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	@Override
	public final Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.objectType = data.addPtr("object_type", OBJECT_TYPE);
		allocateValueMethods(data);
	}

	@Override
	protected void fill() {

		final ObjectIR ascendantIR =
				getBodyIR().getAscendant().ir(getGenerator());

		this.objectType.setConstant(true).setValue(
				ascendantIR.getTypeIR().getObjectType()
				.data(getGenerator()).getPointer());
	}

	private static ID buildId(ObjectBodyIR bodyIR) {

		final ObjectIR objectIR = bodyIR.getObjectIR();
		final Obj ascendant = bodyIR.getAscendant();
		final ID localId;

		if (bodyIR.isMain()) {
			localId = METHODS_ID;
		} else {
			localId = METHODS_ID.detail(
					ascendant.ir(objectIR.getGenerator()).getId());
		}

		return objectIR.getId().setLocal(localId);
	}

	private void allocateValueMethods(SubData<Op> data) {

		final Obj ascendant = getBodyIR().getAscendant();
		final ValueStruct<?, ?> valueStruct =
				ascendant.value().getValueStruct();
		final ValueType<?> valueType = valueStruct.getValueType();
		final Obj typeObject =
				valueType.typeObject(ascendant.getContext().getIntrinsics());

		if (ascendant == typeObject) {
			getObjectIR().getValueIR().allocateMethods(this, data);
		}
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ObjectMethodsIR getType() {
			return (ObjectMethodsIR) super.getType();
		}

		public final StructRecOp<ObjectIRType.Op> objectType(Code code) {
			return ptr(null, code, getType().objectType);
		}

	}

}

/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.type;

import static org.o42a.core.ir.object.type.ObjectIRDesc.OBJECT_DESC_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.object.Obj;
import org.o42a.core.source.Intrinsics;
import org.o42a.util.string.ID;


public class ObjectDescIR {

	public static final ID OBJECT_DESC_ID = ID.id("desc");

	public static ObjectDescIR allocateDescIR(ObjectIR objectIR) {
		if (!objectIR.isSampleDeclaration()) {
			return objectIR.getSampleDeclaration()
					.ir(objectIR.getGenerator())
					.getDescIR();
		}
		return new ObjectDescIR(objectIR);
	}

	private final ObjectIR objectIR;
	private Ptr<ObjectIRDescOp> ptr;

	private ObjectDescIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
	}

	public Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Ptr<ObjectIRDescOp> ptr() {
		if (this.ptr != null) {
			return this.ptr;
		}
		return this.ptr = allocate();
	}

	private Ptr<ObjectIRDescOp> allocate() {

		final ObjectIR objectIR = getObjectIR();
		final Obj object = objectIR.getObject();
		final Intrinsics intrinsics = object.getContext().getIntrinsics();

		if (object.is(intrinsics.getVoid())) {
			return getGenerator()
					.externalGlobal()
					.setConstant()
					.link("o42a_obj_void_desc", OBJECT_DESC_TYPE);
		}
		if (object.is(intrinsics.getFalse())) {
			return getGenerator()
					.externalGlobal()
					.setConstant()
					.link("o42a_obj_false_desc", OBJECT_DESC_TYPE);
		}
		if (object.is(intrinsics.getNone())) {
			return getGenerator()
					.externalGlobal()
					.setConstant()
					.link("o42a_obj_none_desc", OBJECT_DESC_TYPE);
		}

		return getGenerator().newGlobal().struct(
				new ObjectDescIRStruct(this))
				.getInstance()
				.getDesc()
				.pointer(getGenerator());
	}

	private static final class ObjectDescIRStruct extends Struct<Op> {

		private final ObjectDescIR descIR;
		private ObjectIRDesc desc;

		ObjectDescIRStruct(ObjectDescIR descIR) {
			super(descIR.getObjectIR().getId().detail(OBJECT_DESC_ID));
			this.descIR = descIR;
		}

		public final ObjectDescIR getDescIR() {
			return this.descIR;
		}

		public final ObjectIR getObjectIR() {
			return getDescIR().getObjectIR();
		}

		public final ObjectIRDesc getDesc() {
			return this.desc;
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.desc = data.addInstance(OBJECT_DESC_ID, OBJECT_DESC_TYPE);
			allocateFieldDecls();
			getDesc().ascendants().addAll(getObjectIR().getBodyIRs());
			getDesc().fields().allocateItems(data);
			getDesc().ascendants().allocateItems(data);
		}

		@Override
		protected void fill() {

			final Generator generator = getGenerator();
			final ObjectIRDesc desc = getDesc();

			desc.valueType()
			.setConstant(true)
			.setValue(
					getObjectIR()
					.getValueIR()
					.getValueTypeIR()
					.getValueTypeDesc());

			desc.objectSize()
			.setConstant(true)
			.setLowLevel(true)
			.setValue(
					() -> getObjectIR()
					.getStruct()
					.layout(generator)
					.size());
		}

		private void allocateFieldDecls() {

			final RelList<FieldDescIR> fields = getDesc().fields();

			for (Fld<?> fld :
					getObjectIR().getMainBodyIR().getDeclaredFields()) {
				if (!fld.isStateless()) {
					fields.add(new FieldDescIR(fld));
				}
			}
		}

	}

	private static final class Op extends StructOp<Op> {

		Op(StructWriter<Op> writer) {
			super(writer);
		}

	}

}

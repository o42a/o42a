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

import static org.o42a.core.ir.object.AscendantDescIR.ASCENDANT_DESC_IR;
import static org.o42a.core.ir.object.ObjectType.OBJECT_TYPE;
import static org.o42a.core.ir.object.SampleDescIR.SAMPLE_DESC_IR;
import static org.o42a.core.ir.op.ObjectCondFunc.OBJECT_COND;
import static org.o42a.core.ir.op.ObjectRefFunc.OBJECT_REF;
import static org.o42a.core.ir.op.ObjectValFunc.OBJECT_VAL;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.op.*;


public final class ObjectDataType extends Type<ObjectDataType.Op> {

	public static final int OBJ_FLAG_RT = 0x1;
	public static final int OBJ_FLAG_ABSTRACT = 0x2;
	public static final int OBJ_FLAG_PROTOTYPE = 0x4;
	public static final int OBJ_FLAG_VOID = 0x80000000;
	public static final int OBJ_FLAG_FALSE = 0x40000000;

	public static final ObjectDataType OBJECT_DATA_TYPE = new ObjectDataType();

	private RelPtrRec object;
	private Int32rec flags;
	private RelPtrRec start;
	private Int32rec allBodiesLayout;
	private ValOp.Type value;
	private CodeRec<ObjectValFunc> valueFunc;
	private CodeRec<ObjectCondFunc> requirementFunc;
	private CodeRec<ObjectValFunc> claimFunc;
	private CodeRec<ObjectCondFunc> conditionFunc;
	private CodeRec<ObjectValFunc> propositionFunc;
	private CodeRec<ObjectRefFunc> ancestorFunc;
	private StructPtrRec<ObjectType.Op> ancestorType;
	private StructPtrRec<ObjectType.Op> ownerType;
	private RelList<ObjectBodyIR> ascendants;
	private RelList<ObjectBodyIR> samples;

	private ObjectDataType() {
	}

	public final RelPtrRec object() {
		return this.object;
	}

	public final Int32rec flags() {
		return this.flags;
	}

	public final RelPtrRec start() {
		return this.start;
	}

	public final Int32rec allBodiesLayout() {
		return this.allBodiesLayout;
	}

	public final ValOp.Type value() {
		return this.value;
	}

	public final CodeRec<ObjectValFunc> valueFunc() {
		return this.valueFunc;
	}

	public final CodeRec<ObjectCondFunc> requirementFunc() {
		return this.requirementFunc;
	}

	public final CodeRec<ObjectValFunc> claimFunc() {
		return this.claimFunc;
	}

	public final CodeRec<ObjectCondFunc> conditionFunc() {
		return this.conditionFunc;
	}

	public final CodeRec<ObjectValFunc> propositionFunc() {
		return this.propositionFunc;
	}

	public final StructPtrRec<ObjectType.Op> ownerType() {
		return this.ownerType;
	}

	public final CodeRec<ObjectRefFunc> ancestorFunc() {
		return this.ancestorFunc;
	}

	public final StructPtrRec<ObjectType.Op> ancestorType() {
		return this.ancestorType;
	}

	public final RelList<ObjectBodyIR> ascendants() {
		return this.ascendants;
	}

	public final RelList<ObjectBodyIR> samples() {
		return this.samples;
	}

	@Override
	public ObjectDataType.Op op(StructWriter writer) {
		return new ObjectDataType.Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("ObjectData");
	}

	@Override
	protected void allocate(SubData<ObjectDataType.Op> data) {

		final Generator generator = data.getGenerator();

		this.object = data.addRelPtr("object");
		this.flags = data.addInt32("flags");
		this.start = data.addRelPtr("start");
		this.allBodiesLayout = data.addInt32("all_bodies_layout");
		this.value = data.addInstance(generator.id("value"), VAL_TYPE);
		this.valueFunc = data.addCodePtr("value_f", OBJECT_VAL);
		this.requirementFunc = data.addCodePtr("requirement_f", OBJECT_COND);
		this.claimFunc = data.addCodePtr("claim_f", OBJECT_VAL);
		this.conditionFunc = data.addCodePtr("condition_f", OBJECT_COND);
		this.propositionFunc = data.addCodePtr("proposition_f", OBJECT_VAL);
		this.ownerType = data.addPtr("owner_type", OBJECT_TYPE);
		this.ancestorFunc = data.addCodePtr("ancestor_f", OBJECT_REF);
		this.ancestorType = data.addPtr("ancestor_type", OBJECT_TYPE);
		this.ascendants = new Ascendants().allocate(data, "ascendants");
		this.samples = new Samples().allocate(data, "samples");
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final ObjectDataType getType() {
			return (ObjectDataType) super.getType();
		}

		public final DataOp<RelOp> object(Code code) {
			return writer().relPtr(code, getType().object());
		}

		public final AnyOp objectPtr(Code code) {
			return object(code).load(code).offset(code, this);
		}

		public final DataOp<RelOp> start(Code code) {
			return writer().relPtr(code, getType().start());
		}

		public final AnyOp startPtr(Code code) {
			return start(code).load(code).offset(code, this);
		}

		public final ValOp value(Code code) {
			return writer().struct(code, getType().value());
		}

		public final CodeOp<ObjectValFunc> valueFunc(Code code) {
			return writer().func(code, getType().valueFunc());
		}

		public final CodeOp<ObjectCondFunc> requirementFunc(Code code) {
			return writer().func(code, getType().requirementFunc());
		}

		public final CodeOp<ObjectValFunc> claimFunc(Code code) {
			return writer().func(code, getType().claimFunc());
		}

		public final CodeOp<ObjectCondFunc> conditionFunc(Code code) {
			return writer().func(code, getType().conditionFunc());
		}

		public final CodeOp<ObjectValFunc> propositionFunc(Code code) {
			return writer().func(code, getType().propositionFunc());
		}

		public final CodeOp<ObjectRefFunc> ancestorFunc(Code code) {
			return writer().func(code, getType().ancestorFunc());
		}

		public final DataOp<ObjectType.Op> ancestorType(Code code) {
			return writer().ptr(code, getType().ancestorType());
		}

		@Override
		public String toString() {
			return getType() + " data";
		}

	}

	private static final class Ascendants extends RelList<ObjectBodyIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				ObjectBodyIR item) {

			final Generator generator = item.getGenerator();
			final CodeId id =
				generator.id("ascendant")
				.detail(item.getAscendant().ir(generator).getId());
			final AscendantDescIR.Type desc = data.addInstance(
					id,
					ASCENDANT_DESC_IR,
					new AscendantDescIR(item));

			return desc.data(data.getGenerator()).getPointer();
		}

	}

	private static final class Samples extends RelList<ObjectBodyIR> {

		@Override
		protected Ptr<?> allocateItem(
				SubData<?> data,
				int index,
				ObjectBodyIR item) {

			final Generator generator = item.getGenerator();
			final CodeId id =
				generator.id("sample")
				.detail(item.getAscendant().ir(generator).getId());
			final SampleDescIR.Type desc = data.addInstance(
					id,
					SAMPLE_DESC_IR,
					new SampleDescIR(item));

			return desc.data(data.getGenerator()).getPointer();
		}

	}

}

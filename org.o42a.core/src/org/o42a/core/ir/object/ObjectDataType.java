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
	private ValOp.Type value;
	private FuncRec<ObjectValFunc> valueFunc;
	private FuncRec<ObjectCondFunc> requirementFunc;
	private FuncRec<ObjectValFunc> claimFunc;
	private FuncRec<ObjectCondFunc> conditionFunc;
	private FuncRec<ObjectValFunc> propositionFunc;
	private FuncRec<ObjectRefFunc> ancestorFunc;
	private StructRec<ObjectType.Op> ancestorType;
	private StructRec<ObjectType.Op> ownerType;
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

	public final ValOp.Type value() {
		return this.value;
	}

	public final FuncRec<ObjectValFunc> valueFunc() {
		return this.valueFunc;
	}

	public final FuncRec<ObjectCondFunc> requirementFunc() {
		return this.requirementFunc;
	}

	public final FuncRec<ObjectValFunc> claimFunc() {
		return this.claimFunc;
	}

	public final FuncRec<ObjectCondFunc> conditionFunc() {
		return this.conditionFunc;
	}

	public final FuncRec<ObjectValFunc> propositionFunc() {
		return this.propositionFunc;
	}

	public final StructRec<ObjectType.Op> ownerType() {
		return this.ownerType;
	}

	public final FuncRec<ObjectRefFunc> ancestorFunc() {
		return this.ancestorFunc;
	}

	public final StructRec<ObjectType.Op> ancestorType() {
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
		this.value = data.addInstance(generator.id("value"), VAL_TYPE);
		this.valueFunc = data.addFuncPtr("value_f", OBJECT_VAL);
		this.requirementFunc = data.addFuncPtr("requirement_f", OBJECT_COND);
		this.claimFunc = data.addFuncPtr("claim_f", OBJECT_VAL);
		this.conditionFunc = data.addFuncPtr("condition_f", OBJECT_COND);
		this.propositionFunc = data.addFuncPtr("proposition_f", OBJECT_VAL);
		this.ownerType = data.addPtr("owner_type", OBJECT_TYPE);
		this.ancestorFunc = data.addFuncPtr("ancestor_f", OBJECT_REF);
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

		public final RecOp<RelOp> object(Code code) {
			return relPtr(null, code, getType().object());
		}

		public final DataOp loadObject(Code code) {
			return object(code).load(null, code).offset(null, code, this).toData(null, code);
		}

		public final RecOp<RelOp> start(Code code) {
			return relPtr(null, code, getType().start());
		}

		public final DataOp loadStart(Code code) {
			return start(code).load(null, code).offset(null, code, this).toData(null, code);
		}

		public final ValOp value(Code code) {
			return struct(null, code, getType().value());
		}

		public final FuncOp<ObjectValFunc> valueFunc(Code code) {
			return func(null, code, getType().valueFunc());
		}

		public final FuncOp<ObjectCondFunc> requirementFunc(Code code) {
			return func(null, code, getType().requirementFunc());
		}

		public final FuncOp<ObjectValFunc> claimFunc(Code code) {
			return func(null, code, getType().claimFunc());
		}

		public final FuncOp<ObjectCondFunc> conditionFunc(Code code) {
			return func(null, code, getType().conditionFunc());
		}

		public final FuncOp<ObjectValFunc> propositionFunc(Code code) {
			return func(null, code, getType().propositionFunc());
		}

		public final FuncOp<ObjectRefFunc> ancestorFunc(Code code) {
			return func(null, code, getType().ancestorFunc());
		}

		public final RecOp<ObjectType.Op> ancestorType(Code code) {
			return ptr(null, code, getType().ancestorType());
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

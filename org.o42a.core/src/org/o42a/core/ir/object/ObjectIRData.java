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

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.op.ObjectDataFunc.OBJECT_DATA;
import static org.o42a.core.ir.object.type.AscendantDescIR.ASCENDANT_DESC_IR;
import static org.o42a.core.ir.object.type.SampleDescIR.SAMPLE_DESC_IR;
import static org.o42a.core.ir.object.type.ValueTypeDescOp.VALUE_TYPE_DESC_TYPE;
import static org.o42a.core.ir.object.value.ObjectValueFunc.OBJECT_VALUE;
import static org.o42a.core.ir.system.MutexSystemType.MUTEX_SYSTEM_TYPE;
import static org.o42a.core.ir.system.ThreadCondSystemType.THREAD_COND_SYSTEM_TYPE;
import static org.o42a.core.ir.system.ThreadSystemType.THREAD_SYSTEM_TYPE;
import static org.o42a.core.ir.value.ObjectValFunc.OBJECT_VAL;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.op.ObjectDataFunc;
import org.o42a.core.ir.object.type.AscendantDescIR;
import org.o42a.core.ir.object.type.SampleDescIR;
import org.o42a.core.ir.object.type.ValueTypeDescOp;
import org.o42a.core.ir.object.value.ObjectValueFunc;
import org.o42a.core.ir.op.RelList;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.ValType;


public final class ObjectIRData extends Type<ObjectIRData.Op> {

	public static final short OBJ_FLAG_RT = 0x1;
	public static final short OBJ_FLAG_ABSTRACT = 0x2;
	public static final short OBJ_FLAG_PROTOTYPE = 0x4;
	public static final short OBJ_FLAG_VOID = ~0x7fff;
	public static final short OBJ_FLAG_FALSE = 0x4000;

	public static final ObjectIRData OBJECT_DATA_TYPE = new ObjectIRData();

	private static final Type<?>[] TYPE_DEPENDENCIES =
			new Type<?>[] {OBJECT_TYPE};

	private RelRec object;
	private RelRec start;
	private Int16rec flags;
	private ValType value;
	private FuncRec<ObjectValueFunc> valueFunc;
	private FuncRec<ObjectValFunc> claimFunc;
	private FuncRec<ObjectValFunc> propositionFunc;
	private StructRec<ValueTypeDescOp> valueType;
	private RelList<ObjectBodyIR> ascendants;
	private RelList<ObjectBodyIR> samples;

	private ObjectIRData() {
	}

	@Override
	public final Type<?>[] getTypeDependencies() {
		return TYPE_DEPENDENCIES;
	}

	public final RelRec object() {
		return this.object;
	}

	public final RelRec start() {
		return this.start;
	}

	public final Int16rec flags() {
		return this.flags;
	}

	public final ValType value() {
		return this.value;
	}

	public final FuncRec<ObjectValueFunc> valueFunc() {
		return this.valueFunc;
	}

	public final FuncRec<ObjectValFunc> claimFunc() {
		return this.claimFunc;
	}

	public final FuncRec<ObjectValFunc> propositionFunc() {
		return this.propositionFunc;
	}

	public final StructRec<ValueTypeDescOp> valueType() {
		return this.valueType;
	}

	public final RelList<ObjectBodyIR> ascendants() {
		return this.ascendants;
	}

	public final RelList<ObjectBodyIR> samples() {
		return this.samples;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.rawId("o42a_obj_data_t");
	}

	@Override
	protected void allocate(SubData<Op> data) {

		final Generator generator = data.getGenerator();

		this.object = data.addRelPtr("object");
		this.start = data.addRelPtr("start");
		this.flags = data.addInt16("flags");
		data.addInt8("mutex_init").setValue((byte) 0);
		data.addInt8("value_calc").setValue((byte) 0);
		data.addSystem("value_thread", THREAD_SYSTEM_TYPE);
		data.addSystem("mutex", MUTEX_SYSTEM_TYPE);
		data.addSystem("thread_cond", THREAD_COND_SYSTEM_TYPE);
		this.value = data.addInstance(generator.id("value"), ValType.VAL_TYPE);
		this.valueFunc = data.addFuncPtr("value_f", OBJECT_VALUE);
		this.claimFunc = data.addFuncPtr("claim_f", OBJECT_VAL);
		this.propositionFunc = data.addFuncPtr("proposition_f", OBJECT_VAL);
		this.valueType = data.addPtr("value_type", VALUE_TYPE_DESC_TYPE);
		data.addPtr("fld_ctrs", FLD_CTR_TYPE).setNull();
		this.ascendants = new Ascendants().allocate(data, "ascendants");
		this.samples = new Samples().allocate(data, "samples");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0100);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ObjectIRData getType() {
			return (ObjectIRData) super.getType();
		}

		public final RelRecOp object(Code code) {
			return relPtr(null, code, getType().object());
		}

		public final DataOp loadObject(Code code) {
			return object(code)
					.load(null, code)
					.offset(
							code.id("main_body").type(code.id("any")),
							code,
							this)
					.toData(code.id("main_body"), code);
		}

		public final RelRecOp start(Code code) {
			return relPtr(null, code, getType().start());
		}

		public final DataOp loadStart(Code code) {
			return start(code)
					.load(null, code)
					.offset(
							code.id("object_start").type(code.id("any")),
							code,
							this)
					.toData(code.id("object_start"), code);
		}

		public final ValType.Op value(Code code) {
			return struct(null, code, getType().value());
		}

		public final FuncOp<ObjectValueFunc> valueFunc(Code code) {
			return func(null, code, getType().valueFunc());
		}

		public final FuncOp<ObjectValFunc> claimFunc(Code code) {
			return func(null, code, getType().claimFunc());
		}

		public final FuncOp<ObjectValFunc> propositionFunc(Code code) {
			return func(null, code, getType().propositionFunc());
		}

		public final void lock(Code code) {

			final FuncPtr<ObjectDataFunc> fn =
					code.getGenerator().externalFunction().link(
							"o42a_obj_lock",
							OBJECT_DATA);

			fn.op(null, code).call(code, this);
		}

		public final void unlock(Code code) {

			final FuncPtr<ObjectDataFunc> fn =
					code.getGenerator().externalFunction().link(
							"o42a_obj_unlock",
							OBJECT_DATA);

			fn.op(null, code).call(code, this);
		}

		public final void wait(Code code) {

			final FuncPtr<ObjectDataFunc> fn =
					code.getGenerator().externalFunction().link(
							"o42a_obj_wait",
							OBJECT_DATA);

			fn.op(null, code).call(code, this);
		}

		public final void signal(Code code) {

			final FuncPtr<ObjectDataFunc> fn =
					code.getGenerator().externalFunction().link(
							"o42a_obj_signal",
							OBJECT_DATA);

			fn.op(null, code).call(code, this);
		}

		public final void broadcast(Code code) {

			final FuncPtr<ObjectDataFunc> fn =
					code.getGenerator().externalFunction().link(
							"o42a_obj_broadcast",
							OBJECT_DATA);

			fn.op(null, code).call(code, this);
		}

		@Override
		public String toString() {
			return getType() + " data";
		}

		@Override
		protected CodeId fieldId(Code code, CodeId local) {
			return code.id("object_data").setLocal(local);
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

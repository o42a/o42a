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
package org.o42a.core.ir.object.op;

import static org.o42a.codegen.code.AllocationMode.ALLOCATOR_ALLOCATION;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;
import static org.o42a.core.ir.object.op.NewObjectFn.NEW_OBJECT;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ir.value.ValOp.finalVal;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import java.util.function.Function;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.Allocated;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.VmtIRChain;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValHolderFactory;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class CtrOp extends IROp {

	public static final Type CTR_TYPE = new Type();
	public static final Allocatable<Op> ALLOCATABLE_CTR =
			new AllocatableCtr();

	public static final ID CTR_ID = ID.id("ctr");

	private final Function<Code, Op> ptr;
	private Obj sample;
	private ObjectOp host;

	public CtrOp(CodeBuilder builder, Function<Code, Op> ptr) {
		super(builder);
		this.ptr = ptr;
	}

	public final Obj getSample() {
		return this.sample;
	}

	public final boolean isEager() {
		return getSample().value().getStatefulness().isEager();
	}

	@Override
	public final Op ptr(Code code) {
		return this.ptr.apply(code);
	}

	public final ObjectOp host() {
		return this.host;
	}

	public final ValOp value(
			String name,
			Allocator allocator,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {
		return finalVal(
				name,
				allocator,
				getBuilder(),
				code -> ptr(code).value(code),
				valueType,
				holderFactory);
	}

	public final CtrOp host(ObjectOp host) {
		this.host = host;
		return this;
	}

	public final CtrOp fillOwner(Code code, ObjectOp host) {
		host(host);

		final Op ptr = ptr(code);

		if (host != null) {
			ptr.owner(code).store(code, host.toData(null, code));
		} else {
			ptr.owner(code).store(code, code.nullDataPtr());
		}

		return this;
	}

	public final CtrOp fillAscendants(
			CodeDirs dirs,
			ObjectOp ancestor,
			Obj sample) {

		final Block code = dirs.code();
		final Op ptr = ptr(code);

		ptr.ancestor(code).store(code, ancestor.toData(null, code));
		this.sample = sample;

		if (isEager()) {
			if (code.isDebug()) {
				// Not meaningful for eager objects,
				// NULL is required here only for making memory dumps.
				ptr.sample(code).store(code, code.nullDataPtr());
			}

			final ValOp value = value(
					"eager",
					code.getAllocator(),
					sample.type().getValueType(),
					TEMP_VAL_HOLDER);
			final ValDirs eagerDirs = dirs.nested().value(value);
			final ValOp result = ancestor.value().writeValue(eagerDirs);

			value.store(code, result);

			eagerDirs.done();
		} else {

			final ObjectIR sampleIR = sample.ir(getGenerator());

			ptr.sample(code).store(
					code,
					sampleIR.ptr().op(null, code).toData(null, code));
			ptr.numDeps(code).store(
					code,
					code.int32(sampleIR.existingDeps().size()));

			ptr.value(code)
			.flags(code, NOT_ATOMIC)
			.store(code, VAL_INDEFINITE);
		}

		return this;
	}

	public ObjectOp newObject(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();

		code.dump("Constructing: ", this);

		final DataOp result;

		if (isEager()) {
			result = eagerFunc().op(null, code).newObject(code, this);
		} else {
			result = newFunc().op(null, code).newObject(code, this);
		}

		result.isNull(null, code).go(code, dirs.falseDir());

		final ObjectOp newObject = anonymousObject(dirs, result, getSample());

		if (!isEager()) {
			newObject.fillDeps(dirs, host(), getSample());
		}

		return holder.holdVolatile(code, newObject);
	}

	private FuncPtr<NewObjectFn> newFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_new", NEW_OBJECT);
	}

	private FuncPtr<NewObjectFn> eagerFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_eager", NEW_OBJECT);
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp owner(Code code) {
			return ptr(null, code, getType().owner());
		}

		public final DataRecOp ancestor(Code code) {
			return ptr(null, code, getType().ancestor());
		}

		public final DataRecOp sample(Code code) {
			return ptr(null, code, getType().sample());
		}

		public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
			return ptr(null, code, getType().vmtc());
		}

		public final ValType.Op value(Code code) {
			return struct(null, code, getType().value());
		}

		public final Int32recOp numDeps(Code code) {
			return int32(null, code, getType().numDeps());
		}

		public final CtrOp op(CodeBuilder builder) {
			return new CtrOp(builder, code -> this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec owner;
		private DataRec ancestor;
		private DataRec sample;
		private StructRec<VmtIRChain.Op> vmtc;
		private ValType value;
		private Int32rec numDeps;

		private Type() {
			super(ID.rawId("o42a_obj_ctr_t"));
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		public final DataRec owner() {
			return this.owner;
		}

		public final DataRec ancestor() {
			return this.ancestor;
		}

		public final DataRec sample() {
			return this.sample;
		}

		public final StructRec<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		public final ValType value() {
			return this.value;
		}

		public final Int32rec numDeps() {
			return this.numDeps;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.owner = data.addDataPtr("owner");
			this.ancestor = data.addDataPtr("ancestor");
			this.sample = data.addDataPtr("sample");
			this.vmtc = data.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
			this.value = data.addInstance(ID.rawId("value"), VAL_TYPE);
			this.numDeps = data.addInt32("num_deps");
		}

		@Override
		protected DebugTypeInfo createTypeInfo() {
			return externalTypeInfo(0x042a0120);
		}

	}

	private static final class AllocatableCtr implements Allocatable<Op> {

		@Override
		public AllocationMode getAllocationMode() {
			return ALLOCATOR_ALLOCATION;
		}

		@Override
		public int getDisposePriority() {
			return NORMAL_DISPOSE_PRIORITY;
		}

		@Override
		public Op allocate(Allocations code, Allocated<Op> allocated) {
			return code.allocate(CTR_ID, CTR_TYPE);
		}

		@Override
		public void init(Code code, Op allocated) {
			allocated.vmtc(code).store(code, code.nullPtr(VMT_IR_CHAIN_TYPE));
		}

		@Override
		public void dispose(Code code, Allocated<Op> allocated) {
		}

	}

}

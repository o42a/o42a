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
import static org.o42a.core.ir.object.ObjectOp.objectAncestor;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.op.AllocateObjectFn.ALLOCATE_OBJECT;
import static org.o42a.core.ir.object.op.DisposeObjectFn.DISPOSE_OBJECT;
import static org.o42a.core.ir.object.op.NewObjectFn.NEW_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.object.vmt.VmtIRChain.VMT_IR_CHAIN_TYPE;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
import static org.o42a.core.ir.value.ValHolderFactory.VAL_TRAP;
import static org.o42a.core.ir.value.ValOp.finalVal;

import java.util.function.Function;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.desc.ObjectDescIR;
import org.o42a.core.ir.object.vmt.VmtIR;
import org.o42a.core.ir.object.vmt.VmtIRChain;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ObjectValueFn;
import org.o42a.core.ir.value.ValHolderFactory;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class CtrOp extends IROp<CtrOp.Op> {

	public static final Type CTR_TYPE = new Type();
	public static final Allocatable<Op> ALLOCATABLE_CTR =
			new AllocatableCtr();

	public static final ID CTR_ID = ID.id("ctr");

	public static final CtrOp allocateCtr(BuilderCode code) {
		return allocateCtr(code.getBuilder(), code.code());
	}

	public static final CtrOp allocateCtr(CodeBuilder builder, Code code) {
		return new CtrOp(
				builder,
				code.allocate(CTR_ID, ALLOCATABLE_CTR)::get);
	}

	private final Function<Code, Op> ptr;
	private Obj sample;
	private ObjectOp host;
	private ObjectOp ancestor;
	private DataOp objectPtr;

	public CtrOp(CodeBuilder builder, Function<Code, Op> ptr) {
		super(builder);
		this.ptr = ptr;
	}

	public final Obj getSample() {
		return this.sample;
	}

	public final CtrOp sample(Obj sample) {
		this.sample = sample;
		return this;
	}

	public final ObjectOp getAncestor() {
		return this.ancestor;
	}

	public final CtrOp ancestor(ObjectOp ancestor) {
		this.ancestor = ancestor;
		return this;
	}

	public final CtrOp evalAncestor(CodeDirs dirs) {
		return ancestor(objectAncestor(
				dirs,
				host(),
				getSample(),
				tempObjHolder(dirs.getAllocator())));
	}

	public final boolean isEager() {
		return getSample().value().getStatefulness().isEager();
	}

	public final boolean isExplicitEager() {
		return getSample().value().getStatefulness().isExplicitEager();
	}

	@Override
	public final Op ptr(Code code) {
		return this.ptr.apply(code);
	}

	public final ObjectOp host() {
		return this.host;
	}

	public final ObjectOp object(Code code) {
		if (this.objectPtr == null) {
			this.objectPtr = ptr(code).object(code).load(null, code);
		}

		final ObjectIRStruct sampleType =
				getSample().ir(getGenerator()).getType();

		return this.objectPtr.to(null, code, sampleType)
				.op(getBuilder(), getSample(), COMPATIBLE);
	}

	public final ValOp objectValue(
			String name,
			Allocator allocator,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {
		return finalVal(
				name,
				allocator,
				getBuilder(),
				code -> object(code).objectData(code).ptr().value(code),
				valueType,
				holderFactory);
	}

	public final CtrOp allocateObject(CodeDirs dirs) {

		final ObjectDescIR descIR = getSample().ir(getGenerator()).getDescIR();
		final Block code = dirs.code();

		this.objectPtr =
				allocFunc().op(null, code).allocateObject(code, descIR);
		this.objectPtr.isNull(null, code).go(code, dirs.falseDir());
		ptr(code).object(code).store(code, this.objectPtr);

		return this;
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

	public final CtrOp fillAncestor(Code code) {
		ptr(code).ancestor(code).store(code, getAncestor().toData(null, code));
		return this;
	}

	public final CtrOp fillVmtc(Code code) {

		final StructRecOp<VmtIRChain.Op> vmtcRec = ptr(code).vmtc(code);
		final VmtIR vmtIR = getSample().ir(getGenerator()).getVmtIR();

		if (isExplicitEager()) {
			vmtcRec.store(
					code,
					getAncestor().objectData(code)
					.ptr()
					.vmtc(code)
					.load(null, code));
		} else {
			vmtcRec.store(
					code,
					vmtIR.terminator().pointer(getGenerator()).op(null, code));
		}

		return this;
	}

	public final CtrOp fillObject(CodeDirs dirs) {

		final Block dispose = dirs.addBlock("dispose");
		final CodeDirs fillDirs = dirs.setFalseDir(dispose.head());

		fillValue(dirs);
		fillDeps(dirs);

		fillDirs.done();

		if (dispose.exists()) {
			getGenerator()
			.externalFunction()
			.link("o42a_obj_dispose", DISPOSE_OBJECT)
			.op(null, dispose)
			.dispose(dispose, this);

			dispose.go(dirs.falseDir());
		}

		return this;
	}

	public final ObjectOp newObject(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();

		code.dump("Constructing: ", this);

		this.objectPtr = newFunc().op(null, code).newObject(code, this);
		this.objectPtr.isNull(null, code).go(code, dirs.falseDir());

		final ObjectOp newObject = object(dirs.code());

		return holder.holdVolatile(code, newObject);
	}

	private FuncPtr<AllocateObjectFn> allocFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_alloc", ALLOCATE_OBJECT);
	}

	private FuncPtr<NewObjectFn> newFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_new", NEW_OBJECT);
	}

	private void fillValue(CodeDirs dirs) {

		final ObjectOp ancestor = getAncestor();
		final Obj sample = getSample();
		final Block code = dirs.code();
		final ObjectValueFn valueFn;

		if (!isEager()) {

			final ObjectIR sampleIR = sample.ir(getGenerator());
			final ObjectValueIR valueIR = sampleIR.getObjectValueIR();

			if (sample.value().getDefinitions().areInherited()) {
				valueFn =
						getAncestor()
						.objectData(code)
						.ptr()
						.valueFunc(code)
						.load(null, code);
			} else {
				valueFn = valueIR.ptr().op(null, code);
			}
		} else {
			valueFn =
					getGenerator()
					.externalFunction()
					.link("o42a_obj_value_eager", OBJECT_VALUE)
					.op(null, code);

			final ValOp value = objectValue(
					"eager",
					code.getAllocator(),
					sample.type().getValueType(),
					VAL_TRAP);

			final ValDirs eagerDirs = dirs.nested().value(value);
			final ValOp result = ancestor.value().writeValue(eagerDirs);

			value.store(dirs.code(), result);
			eagerDirs.done();
		}

		object(code)
		.objectData(code)
		.ptr()
		.valueFunc(code)
		.store(code, valueFn);
	}

	private void fillDeps(CodeDirs dirs) {
		if (!isExplicitEager()) {
			object(dirs.code()).fillDeps(dirs, host(), getSample());
		}
	}

	public static final class Op extends StructOp<Op> {

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final Type getType() {
			return (Type) super.getType();
		}

		public final DataRecOp object(Code code) {
			return ptr(null, code, getType().object());
		}

		public final DataRecOp owner(Code code) {
			return ptr(null, code, getType().owner());
		}

		public final DataRecOp ancestor(Code code) {
			return ptr(null, code, getType().ancestor());
		}

		public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
			return ptr(null, code, getType().vmtc());
		}

		public final CtrOp op(CodeBuilder builder) {
			return new CtrOp(builder, code -> this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec object;
		private DataRec owner;
		private DataRec ancestor;
		private StructRec<VmtIRChain.Op> vmtc;

		private Type() {
			super(ID.rawId("o42a_obj_ctr_t"));
		}

		@Override
		public Op op(StructWriter<Op> writer) {
			return new Op(writer);
		}

		public final DataRec object() {
			return this.object;
		}

		public final DataRec owner() {
			return this.owner;
		}

		public final DataRec ancestor() {
			return this.ancestor;
		}

		public final StructRec<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addDataPtr("object");
			this.owner = data.addDataPtr("owner");
			this.ancestor = data.addDataPtr("ancestor");
			this.vmtc = data.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
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
		public Op allocate(Allocations code, Allocated<Op> allocated) {
			return code.allocate(CTR_ID, CTR_TYPE);
		}

		@Override
		public void init(Code code, Op allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<Op> allocated) {
		}

	}

}

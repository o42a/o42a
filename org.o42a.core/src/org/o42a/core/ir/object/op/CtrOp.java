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
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectOp.objectAncestor;
import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;
import static org.o42a.core.ir.object.op.NewObjectFn.NEW_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.object.type.ObjectIRDesc.OBJECT_DESC_TYPE;
import static org.o42a.core.ir.value.ObjectValueFn.OBJECT_VALUE;
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
import org.o42a.core.ir.object.*;
import org.o42a.core.ir.object.type.ObjectIRDescOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.*;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class CtrOp extends IROp {

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

	public final CtrOp fillAscendants(CodeDirs dirs, Obj sample) {

		final ObjectOp ancestor = objectAncestor(
				dirs,
				host(),
				sample,
				tempObjHolder(dirs.getAllocator()));

		return fillAscendants(dirs, ancestor, sample);
	}

	public final CtrOp fillAscendants(
			CodeDirs dirs,
			ObjectOp ancestor,
			Obj sample) {
		this.sample = sample;

		final Block code = dirs.code();
		final Op ptr = ptr(code);

		ptr.ancestor(code).store(code, ancestor.toData(null, code));

		if (isExplicitEager()) {
			ptr.sampleDesc(code)
			.store(code, code.nullPtr(OBJECT_DESC_TYPE));
			if (code.isDebug()) {
				// Not meaningful for eager objects.
				// NULLs required here only for making memory dumps.
				ptr.sampleTypeInfo(code).store(code, code.nullPtr());
				ptr.valueFunc(code).store(code, code.nullPtr(OBJECT_VALUE));
				ptr.defFunc(code).store(code, code.nullPtr(OBJECT_VALUE));
			}
		} else {

			final ObjectIR sampleIR = sample.ir(getGenerator());
			final ObjectValueIR valueIR = sampleIR.getObjectValueIR();

			ptr.sampleDesc(code).store(
					code,
					sampleIR.getDescIR().ptr().op(null, code));
			if (code.isDebug()) {
				ptr.sampleTypeInfo(code).store(
						code,
						sampleIR.getType()
						.allocate()
						.getTypeInfo()
						.getPointer()
						.op(null, code));
			}
			ptr.valueFunc(code).store(
					code,
					valueIR.value().get().op(null, code));
			if (sample.value().getDefinitions().areInherited()) {
				ptr.defFunc(code).store(code, code.nullPtr(OBJECT_VALUE));
			} else {
				ptr.defFunc(code).store(
						code,
						valueIR.def().get().op(null, code));
			}
		}

		if (isEager()) {

			final ValOp value = value(
					"eager",
					code.getAllocator(),
					sample.type().getValueType(),
					TEMP_VAL_HOLDER);
			final ValDirs eagerDirs = dirs.nested().value(value);
			final ValOp result = ancestor.value().writeValue(eagerDirs);

			value.store(code, result);

			eagerDirs.done();
		}

		return this;
	}

	public final CtrOp fillVmtc(Code code) {

		final StructRecOp<VmtIRChain.Op> vmtcRec = ptr(code).vmtc(code);
		final VmtIR vmtIR = getSample().ir(getGenerator()).getVmtIR();

		if (!isExplicitEager()) {
			vmtcRec.store(
					code,
					vmtIR.terminator().pointer(getGenerator()).op(null, code));
		} else if (code.isDebug()) {
			// Not meaningful for eager objects.
			// NULLs required here only for making memory dumps.
			vmtcRec.store(code, code.nullPtr(VMT_IR_CHAIN_TYPE));
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

		if (!isExplicitEager()) {
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

		public final StructRecOp<ObjectIRDescOp> sampleDesc(Code code) {
			return ptr(null, code, getType().sample());
		}

		public final AnyRecOp sampleTypeInfo(Code code) {
			return ptr(null, code, getType().sampleTypeInfo());
		}

		public final FuncOp<ObjectValueFn> valueFunc(Code code) {
			return func(null, code, getType().valueFunc());
		}

		public final FuncOp<ObjectValueFn> defFunc(Code code) {
			return func(null, code, getType().defFunc());
		}

		public final StructRecOp<VmtIRChain.Op> vmtc(Code code) {
			return ptr(null, code, getType().vmtc());
		}

		public final ValType.Op value(Code code) {
			return struct(null, code, getType().value());
		}

		public final CtrOp op(CodeBuilder builder) {
			return new CtrOp(builder, code -> this);
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec owner;
		private DataRec ancestor;
		private StructRec<ObjectIRDescOp> sampleDesc;
		private AnyRec sampleTypeInfo;
		private FuncRec<ObjectValueFn> valueFunc;
		private FuncRec<ObjectValueFn> defFunc;
		private StructRec<VmtIRChain.Op> vmtc;
		private ValType value;

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

		public final StructRec<ObjectIRDescOp> sample() {
			return this.sampleDesc;
		}

		public final StructRec<ObjectIRDescOp> sampleDesc() {
			return this.sampleDesc;
		}

		public final AnyRec sampleTypeInfo() {
			return this.sampleTypeInfo;
		}

		public final FuncRec<ObjectValueFn> valueFunc() {
			return this.valueFunc;
		}

		public final FuncRec<ObjectValueFn> defFunc() {
			return this.defFunc;
		}

		public final StructRec<VmtIRChain.Op> vmtc() {
			return this.vmtc;
		}

		public final ValType value() {
			return this.value;
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.owner = data.addDataPtr("owner");
			this.ancestor = data.addDataPtr("ancestor");
			this.sampleDesc = data.addPtr("sample_desc", OBJECT_DESC_TYPE);
			if (data.getGenerator().isDebug()) {
				this.sampleTypeInfo = data.addPtr("sample_type_info");
			}
			this.valueFunc = data.addFuncPtr("value_f", OBJECT_VALUE);
			this.defFunc = data.addFuncPtr("def_f", OBJECT_VALUE);
			this.vmtc = data.addPtr("vmtc", VMT_IR_CHAIN_TYPE);
			this.value = data.addNewInstance(ID.rawId("value"), VAL_TYPE);
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

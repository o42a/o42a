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
import static org.o42a.core.ir.object.op.NewObjectFunc.NEW_OBJECT;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.ir.value.ValOp.finalVal;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.DataRec;
import org.o42a.codegen.data.Int32rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
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

	private final Allocated<Op> ptr;

	public CtrOp(CodeBuilder builder, Allocated<Op> ptr) {
		super(builder);
		this.ptr = ptr;
	}

	@Override
	public final Op ptr(Code code) {
		return this.ptr.get(code);
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

	public ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectOp ancestor,
			ObjOp sample) {

		final CodeDirs subDirs = dirs.begin(
				"new_object",
				"New object: sample=" + sample);
		final Block code = subDirs.code();
		final Op ptr = ptr(code);

		if (owner != null) {
			ptr.owner(code).store(code, owner.toData(null, code));
		} else {
			ptr.owner(code).store(code, code.nullDataPtr());
		}
		ptr.ancestor(code).store(
				code,
				ancestor != null
				? ancestor.toData(null, code)
				: code.nullDataPtr());
		ptr.sample(code).store(code, sample.toData(null, code));
		ptr.numDeps(code).store(
				code,
				code.int32(sample.getObjectIR().existingDeps().size()));

		final DataOp result = newFunc().op(null, code).newObject(code, this);

		result.isNull(null, code).go(code, subDirs.falseDir());

		final ObjectOp newObject = anonymousObject(
				subDirs,
				result,
				sample.getWellKnownType());
		final Block resultCode = subDirs.done().code();

		return holder.holdVolatile(resultCode, newObject);
	}

	public ObjectOp eagerObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectOp ancestor,
			Obj sample) {
		assert ancestor != null :
			"Eager object's ancestor not specified";
		assert sample.deps().size() == 0 :
			"Eager object has run-time dependencies";

		final CodeDirs subDirs = dirs.begin(
				"eager_object",
				"Eager object: ancestor=" + ancestor);
		final Block code = subDirs.code();
		final Op ptr = ptr(code);

		if (owner != null) {
			ptr.owner(code).store(code, owner.toData(null, code));
		} else {
			ptr.owner(code).store(code, code.nullDataPtr());
		}
		ptr.ancestor(code).store(code, ancestor.toData(null, code));

		final DataOp result = eagerFunc().op(null, code).newObject(code, this);

		result.isNull(null, code).go(code, subDirs.falseDir());

		final ObjectOp newObject = anonymousObject(subDirs, result, sample);
		final Block resultCode = subDirs.done().code();

		return holder.holdVolatile(resultCode, newObject);
	}

	private FuncPtr<NewObjectFunc> newFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_new", NEW_OBJECT);
	}

	private FuncPtr<NewObjectFunc> eagerFunc() {
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

		public final ValType.Op value(Code code) {
			return struct(null, code, getType().value());
		}

		public final Int32recOp numDeps(Code code) {
			return int32(null, code, getType().numDeps());
		}

	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private DataRec owner;
		private DataRec ancestor;
		private DataRec sample;
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
			allocated.value(code)
			.flags(code, NOT_ATOMIC)
			.store(code, VAL_INDEFINITE);
		}

		@Override
		public void dispose(Code code, Allocated<Op> allocated) {
		}

	}

}

/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value;

import static org.o42a.codegen.data.TypeAlignment.TYPE_ALIGN_8;
import static org.o42a.core.ir.value.ValUseFunc.VAL_USE;

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.impl.ConstValOp;
import org.o42a.core.ir.value.impl.FinalValOp;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public final class ValType extends Type<ValType.Op> {

	public static final ValType VAL_TYPE = new ValType();

	private Int32rec flags;
	private Int32rec length;
	private Int64rec value;

	private ValType() {
		super(ID.rawId("o42a_val_t"));
	}

	public final Int32rec flags() {
		return this.flags;
	}

	public final Int32rec length() {
		return this.length;
	}

	public final Int64rec value() {
		return this.value;
	}

	@Override
	public TypeAlignment requiredAlignment() {
		return TYPE_ALIGN_8;
	}

	public final ValType setConstant(boolean constant) {
		flags().setConstant(constant);
		length().setConstant(constant);
		value().setConstant(constant);
		return this;
	}

	public final ValType set(Val val) {
		flags().setValue(val.getFlags());
		length().setValue(val.getLength());

		final Ptr<AnyOp> pointer = val.getPointer();

		if (pointer != null) {
			value().setNativePtr(pointer);
		} else {
			value().setValue(val.getValue());
		}

		return this;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.flags = data.addInt32("flags");
		this.length = data.addInt32("length");
		this.value = data.addInt64("value");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0002);
	}

	public static final class Op extends StructOp<Op> {

		private boolean allocatedOnStack;

		private Op(StructWriter<Op> writer) {
			super(writer);
		}

		@Override
		public final ValType getType() {
			return (ValType) super.getType();
		}

		public final ValOp op(
				Allocator allocator,
				CodeBuilder builder,
				ValueType<?> valueType,
				ValHolderFactory holderFactory) {
			return new FinalValOp(
					allocator,
					builder,
					this,
					valueType,
					holderFactory);
		}

		public final ValOp op(CodeBuilder builder, Val constant) {
			return new ConstValOp(builder, this, constant);
		}

		public final ValFlagsOp flags(Code code, Atomicity atomicity) {
			return flags(null, code, atomicity);
		}

		public final ValFlagsOp flags(ID id, Code code, Atomicity atomicity) {
			return new ValFlagsOp(id, code, this, atomicity);
		}

		public final Int32recOp length(ID id, Code code) {
			return int32(id, code, getType().length());
		}

		public final Int64recOp rawValue(ID id, Code code) {
			return int64(
					id != null ? id : getId().sub("raw_value"),
					code,
					getType().value());
		}

		@Override
		public void allocated(Code code, StructOp<?> enclosing) {
			this.allocatedOnStack = true;
			super.allocated(code, enclosing);
		}

		public final void useRefCounted(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_val_use", VAL_USE);

			func.op(null, code).call(code, this);
		}

		public final void unuseRefCounted(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_val_unuse", VAL_USE);

			func.op(null, code).call(code, this);
		}

		public final void useObjectPointer(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_obj_start_val_use", VAL_USE);

			func.op(null, code).call(code, this);
		}

		public final void unuseObjectPointer(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_obj_end_val_use", VAL_USE);

			func.op(null, code).call(code, this);
		}

		public final void useArrayPointer(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_array_start_use", VAL_USE);

			func.op(null, code).call(code, this);
		}

		public final void unuseArrayPointer(Code code) {

			final FuncPtr<ValUseFunc> func =
					code.getGenerator()
					.externalFunction()
					.link("o42a_array_end_use", VAL_USE);

			func.op(null, code).call(code, this);
		}

		final boolean isAllocatedOnStack() {
			return this.allocatedOnStack;
		}

	}

}

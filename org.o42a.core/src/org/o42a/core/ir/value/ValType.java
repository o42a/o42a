/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.ir.value.Val.UNKNOWN_FLAG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.value.ValueStruct;


public final class ValType extends Type<ValType.Op> {

	public static final ValType VAL_TYPE = new ValType();

	private Int32rec flags;
	private Int32rec length;
	private Int64rec value;

	private ValType() {
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

	public final ValType setUnknown() {
		flags().setValue(UNKNOWN_FLAG);
		length().setValue(0);
		value().setValue(0L);
		return this;
	}

	@Override
	public Op op(StructWriter<Op> writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("Val");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		this.flags = data.addInt32("flags");
		this.length = data.addInt32("length");
		this.value = data.addInt64("value");
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
				CodeBuilder builder,
				ValueStruct<?, ?> valueStruct) {
			return new ValOp(builder, this, valueStruct, null);
		}

		public final ValOp op(CodeBuilder builder, Val constant) {
			return new ValOp(
					builder,
					this,
					constant.getValueStruct(),
					constant);
		}

		public final Int32recOp flags(CodeId id, Code code) {
			return int32(id, code, getType().flags());
		}

		public final Int32recOp length(CodeId id, Code code) {
			return int32(id, code, getType().length());
		}

		public final Int64recOp rawValue(CodeId id, Code code) {
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

		final boolean isAllocatedOnStack() {
			return this.allocatedOnStack;
		}

	}

}

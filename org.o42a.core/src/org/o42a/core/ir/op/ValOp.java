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
package org.o42a.core.ir.op;

import static org.o42a.core.ir.op.Val.CONDITION_FLAG;
import static org.o42a.core.ir.op.Val.UNKNOWN_FLAG;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public final class ValOp extends StructOp {

	private ValOp(StructWriter writer) {
		super(writer);
	}

	@Override
	public final Type getType() {
		return (Type) super.getType();
	}

	public final DataOp<Int32op> flags(Code code) {
		return writer().int32(code, getType().getFlags());
	}

	public final BoolOp condition(Code code) {
		return flags(code).load(code).lowestBit(code);
	}

	public final BoolOp unknown(Code code) {
		return flags(code).load(code).lshr(code, 1).lowestBit(code);
	}

	public final BoolOp indefinite(Code code) {
		return flags(code).load(code).lshr(code, 2).lowestBit(code);
	}

	public final DataOp<Int32op> length(Code code) {
		return writer().int32(code, getType().getLength());
	}

	public final DataOp<Int64op> plainValue(Code code) {
		return writer().int64(code, getType().getValue());
	}

	public final AnyOp value(Code code) {
		return plainValue(code).toAny(code);
	}

	public ValOp store(Code code, Val value) {
		flags(code).store(code, code.int32(value.getFlags()));
		if (value.getCondition()) {
			length(code).store(code, code.int32(value.getLength()));

			final Ptr<AnyOp> pointer = value.getPointer();

			if (pointer != null) {
				value(code).toPtr(code).store(code, pointer.op(code));
			} else {
				plainValue(code).store(code, code.int64(value.getValue()));
			}
		}
		return this;
	}

	public final ValOp storeVoid(Code code) {
		flags(code).store(code, code.int32(CONDITION_FLAG));
		return this;
	}

	public final ValOp storeFalse(Code code) {
		flags(code).store(code, code.int32(0));
		return this;
	}

	public final ValOp storeUnknown(Code code) {
		flags(code).store(code, code.int32(UNKNOWN_FLAG));
		return this;
	}

	public final ValOp store(Code code, ValOp value) {
		flags(code).store(code, value.flags(code).load(code));
		length(code).store(code, value.length(code).load(code));
		plainValue(code).store(code, value.plainValue(code).load(code));
		return this;
	}

	@Override
	public ValOp create(StructWriter writer) {
		return new ValOp(writer);
	}

	public static final class Type extends org.o42a.codegen.data.Type<ValOp> {

		private Int32rec flags;
		private Int32rec length;
		private Int64rec value;

		Type(IRGeneratorBase generator) {
			super(generator.id("Val"));
		}

		public final Int32rec getFlags() {
			return this.flags;
		}

		public final Int32rec getLength() {
			return this.length;
		}

		public final Int64rec getValue() {
			return this.value;
		}

		public final Type set(Val val) {
			getFlags().setValue(val.getFlags());
			getLength().setValue(val.getLength());

			final Ptr<AnyOp> pointer = val.getPointer();

			if (pointer != null) {
				getValue().setNativePtr(pointer);
			} else {
				getValue().setValue(val.getValue());
			}
			return this;
		}

		public final Type setUnknown() {
			getFlags().setValue(UNKNOWN_FLAG);
			getLength().setValue(0);
			getValue().setValue(0L);
			return this;
		}

		@Override
		public ValOp op(StructWriter writer) {
			return new ValOp(writer);
		}

		@Override
		protected void allocate(SubData<ValOp> data) {
			this.flags = data.addInt32("flags");
			this.length = data.addInt32("length");
			this.value = data.addInt64("value");
		}

	}
}

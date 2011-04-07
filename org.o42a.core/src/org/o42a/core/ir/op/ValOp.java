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
import static org.o42a.core.ir.op.Val.INDEFINITE_FLAG;
import static org.o42a.core.ir.op.Val.UNKNOWN_FLAG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;


public final class ValOp extends StructOp implements CondOp {

	public static final Type VAL_TYPE = new Type();

	private ValOp(StructWriter writer) {
		super(writer);
	}

	@Override
	public final Type getType() {
		return (Type) super.getType();
	}

	public final RecOp<Int32op> flags(Code code) {
		return int32(null, code, getType().flags());
	}

	@Override
	public final BoolOp loadCondition(Code code) {
		return flags(code).load(null, code).lowestBit(null, code);
	}

	@Override
	public final BoolOp loadUnknown(Code code) {
		return flags(code).load(null, code).lshr(null, code, 1).lowestBit(null, code);
	}

	public final BoolOp loadIndefinite(Code code) {
		return flags(code).load(null, code).lshr(null, code, 2).lowestBit(null, code);
	}

	public final RecOp<Int32op> length(Code code) {
		return int32(null, code, getType().length());
	}

	public final RecOp<Int64op> rawValue(Code code) {
		return int64(null, code, getType().value());
	}

	public final AnyOp value(Code code) {
		return rawValue(code).toAny(null, code);
	}

	public ValOp store(Code code, Val value) {
		flags(code).store(code, code.int32(value.getFlags()));
		if (value.getCondition()) {
			length(code).store(code, code.int32(value.getLength()));

			final Ptr<AnyOp> pointer = value.getPointer();

			if (pointer != null) {
				value(code).toPtr(null, code).store(code, pointer.op(null, code));
			} else {
				rawValue(code).store(code, code.int64(value.getValue()));
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

	public final ValOp storeIndefinite(Code code) {
		flags(code).store(code, code.int32(UNKNOWN_FLAG | INDEFINITE_FLAG));
		return this;
	}

	public final ValOp store(Code code, ValOp value) {
		flags(code).store(code, value.flags(code).load(null, code));
		length(code).store(code, value.length(code).load(null, code));
		rawValue(code).store(code, value.rawValue(code).load(null, code));
		return this;
	}

	@Override
	public final void go(Code code, CodeDirs dirs) {
		dirs.go(code, this);
	}

	public static final class Type extends org.o42a.codegen.data.Type<ValOp> {

		private Int32rec flags;
		private Int32rec length;
		private Int64rec value;

		private Type() {
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

		public final Type set(Val val) {
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

		public final Type setUnknown() {
			flags().setValue(UNKNOWN_FLAG);
			length().setValue(0);
			value().setValue(0L);
			return this;
		}

		@Override
		public ValOp op(StructWriter writer) {
			return new ValOp(writer);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("Val");
		}

		@Override
		protected void allocate(SubData<ValOp> data) {
			this.flags = data.addInt32("flags");
			this.length = data.addInt32("length");
			this.value = data.addInt64("value");
		}

	}
}

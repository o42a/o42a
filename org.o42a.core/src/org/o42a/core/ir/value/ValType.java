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

import static org.o42a.core.ir.value.Val.CONDITION_FLAG;
import static org.o42a.core.ir.value.Val.INDEFINITE_FLAG;
import static org.o42a.core.ir.value.Val.UNKNOWN_FLAG;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.value.ValueType;


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
	public Op op(StructWriter writer) {
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

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public final ValType getType() {
			return (ValType) super.getType();
		}

		public final ValOp op(CodeBuilder builder, ValueType<?> valueType) {
			return new ValOp(builder, this, valueType);
		}

		public final RecOp<Int32op> flags(CodeId id, Code code) {
			return int32(id, code, getType().flags());
		}

		public final BoolOp loadCondition(CodeId id, Code code) {

			final Int32op flags = flags(null, code).load(null, code);

			return flags.lowestBit(
					id != null ? id : getId().sub("condition_flag"),
					code);
		}

		public final BoolOp loadUnknown(CodeId id, Code code) {

			final Int32op flags = flags(null, code).load(null, code);

			return flags.lshr(null, code, 1).lowestBit(
					id != null ? id : getId().sub("unknown_flag"),
					code);
		}

		public final BoolOp loadIndefinite(CodeId id, Code code) {

			final Int32op flags = flags(null, code).load(null, code);

			return flags.lshr(null, code, 2).lowestBit(
					id != null ? id : getId().sub("indefinite_flag"),
					code);
		}

		public final RecOp<Int32op> length(CodeId id, Code code) {
			return int32(id, code, getType().length());
		}

		public final RecOp<Int64op> rawValue(CodeId id, Code code) {
			return int64(id, code, getType().value());
		}

		public final AnyOp value(CodeId id, Code code) {
			return rawValue(
					id != null ? id.detail("raw") : null,
					code).toAny(id, code);
		}

		public final Op storeFalse(Code code) {
			flags(null, code).store(code, code.int32(0));
			return this;
		}

		public final Op storeUnknown(Code code) {
			flags(null, code).store(code, code.int32(UNKNOWN_FLAG));
			return this;
		}

		public final Op storeIndefinite(Code code) {
			flags(null, code).store(
					code,
					code.int32(UNKNOWN_FLAG | INDEFINITE_FLAG));
			return this;
		}

		final Op storeVoid(Code code) {
			flags(null, code).store(code, code.int32(CONDITION_FLAG));
			return this;
		}

		final Op store(Code code, Val value) {
			flags(null, code).store(code, code.int32(value.getFlags()));
			if (value.getCondition()) {
				length(null, code).store(code, code.int32(value.getLength()));

				final Ptr<AnyOp> pointer = value.getPointer();

				if (pointer != null) {
					value(null, code)
					.toPtr(null, code)
					.store(code, pointer.op(null, code));
				} else {
					rawValue(null, code).store(
							code,
							code.int64(value.getValue()));
				}
			}
			return this;
		}

		final Op store(Code code, Op value) {
			if (this == value) {
				return this;
			}
			flags(null, code).store(
					code,
					value.flags(null, code).load(null, code));
			length(null, code).store(
					code,
					value.length(null, code).load(null, code));
			rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code));
			return this;
		}

		final Op store(Code code, Int64op value) {
			rawValue(null, code).store(code, value);
			flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));
			return this;
		}

		final Op store(Code code, Fp64op value) {
			value(null, code).toFp64(null, code).store(code, value);
			flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));
			return this;
		}

	}

}

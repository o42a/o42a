/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.codegen.code.Arg;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.*;


public abstract class IntType<
		T extends Number,
		O extends IntOp<O>,
		R extends IntRecOp<R, O>>
				extends NumType<T, O, R> {

	public static final IntType<Byte, Int8op, Int8recOp> INT8 =
			new IntType<Byte, Int8op, Int8recOp>(
					DataType.INT8,
					Int8op.class,
					Int8recOp.class) {
				@Override
				public Byte cast(Number number) {
					return number.byteValue();
				}
				@Override
				public Int8op op(Code code, Byte value) {
					return code.int8(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Int8op> arg) {
					writer.addInt8(arg);
				}
				@Override
				protected Int8op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Int8op> arg) {
					return writer.int8arg(code, arg);
				}
			};

	public static final IntType<Short, Int16op, Int16recOp> INT16 =
			new IntType<Short, Int16op, Int16recOp>(
					DataType.INT16,
					Int16op.class,
					Int16recOp.class) {
				@Override
				public Short cast(Number value) {
					return value.shortValue();
				}
				@Override
				public Int16op op(Code code, Short value) {
					return code.int16(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Int16op> arg) {
					writer.addInt16(arg);
				}
				@Override
				protected Int16op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Int16op> arg) {
					return writer.int16arg(code, arg);
				}
			};

	public static final IntType<Integer, Int32op, Int32recOp> INT32 =
			new IntType<Integer, Int32op, Int32recOp>(
					DataType.INT32,
					Int32op.class,
					Int32recOp.class) {
				@Override
				public Integer cast(Number value) {
					return value.intValue();
				}
				@Override
				public Int32op op(Code code, Integer value) {
					return code.int32(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Int32op> arg) {
					writer.addInt32(arg);
				}
				@Override
				protected Int32op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Int32op> arg) {
					return writer.int32arg(code, arg);
				}
			};

	public static final IntType<Long, Int64op, Int64recOp> INT64 =
			new IntType<Long, Int64op, Int64recOp>(
					DataType.INT64,
					Int64op.class,
					Int64recOp.class) {
				@Override
				public Long cast(Number value) {
					return value.longValue();
				}
				@Override
				public Int64op op(Code code, Long value) {
					return code.int64(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Int64op> arg) {
					writer.addInt64(arg);
				}
				@Override
				protected Int64op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Int64op> arg) {
					return writer.int64arg(code, arg);
				}
			};

	IntType(
			DataType dataType,
			Class<? extends Op> opType,
			Class<? extends R> recType) {
		super(dataType, opType, recType);
	}

	public final O op(Code code, long value) {
		return op(code, cast(value));
	}

}

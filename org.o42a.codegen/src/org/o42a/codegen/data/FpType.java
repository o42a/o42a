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


public abstract class FpType<
		T extends Number,
		O extends FpOp<O>,
		R extends RecOp<R, O>>
				extends NumType<T, O, R> {

	public static final FpType<Float, Fp32op, Fp32recOp> FP32 =
			new FpType<Float, Fp32op, Fp32recOp>(
					DataType.FP32,
					Fp32op.class,
					Fp32recOp.class) {
				@Override
				public Float cast(Number number) {
					return number.floatValue();
				}
				@Override
				public Fp32op op(Code code, Float value) {
					return code.fp32(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Fp32op> arg) {
					writer.addFp32(arg);
				}
				@Override
				protected Fp32op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Fp32op> arg) {
					return writer.fp32arg(code, arg);
				}
			};

	public static final FpType<Double, Fp64op, Fp64recOp> FP64 =
			new FpType<Double, Fp64op, Fp64recOp>(
					DataType.FP64,
					Fp64op.class,
					Fp64recOp.class) {
				@Override
				public Double cast(Number number) {
					return number.doubleValue();
				}
				@Override
				public Fp64op op(Code code, Double value) {
					return code.fp64(value);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<Fp64op> arg) {
					writer.addFp64(arg);
				}
				@Override
				protected Fp64op arg(
						Code code,
						FuncWriter<?> writer,
						Arg<Fp64op> arg) {
					return writer.fp64arg(code, arg);
				}
			};

	private FpType(
			DataType dataType,
			Class<? extends Op> opType,
			Class<? extends R> recType) {
		super(dataType, opType, recType);
	}

	public final O op(Code code, double value) {
		return op(code, cast(value));
	}

}

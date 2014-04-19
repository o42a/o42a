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


public abstract class PtrType<P extends PtrOp<P>, R extends RecOp<R, P>>
		extends ScalarType<Ptr<P>, P, R> {

	public static final PtrType<AnyOp, AnyRecOp> ANY_PTR =
			new PtrType<AnyOp, AnyRecOp>(
					DataType.PTR,
					AnyOp.class,
					AnyRecOp.class) {
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<AnyOp> arg) {
					writer.addPtr(arg);
				}
				@Override
				protected AnyOp arg(
						Code code,
						FuncWriter<?> writer,
						Arg<AnyOp> arg) {
					return writer.ptrArg(code, arg);
				}
			};

	public static final PtrType<DataOp, DataRecOp> DATA_PTR =
			new PtrType<DataOp, DataRecOp>(
					DataType.DATA_PTR,
					DataOp.class,
					DataRecOp.class) {
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<DataOp> arg) {
					writer.addData(arg);
				}
				@Override
				protected DataOp arg(
						Code code,
						FuncWriter<?> writer,
						Arg<DataOp> arg) {
					return writer.dataArg(code, arg);
				}
			};

	PtrType(
			DataType dataType,
			Class<? extends Op> opType,
			Class<? extends R> recType) {
		super(dataType, opType, recType);
	}

	@Override
	public P op(Code code, Ptr<P> value) {
		return value.op(null, code);
	}

}

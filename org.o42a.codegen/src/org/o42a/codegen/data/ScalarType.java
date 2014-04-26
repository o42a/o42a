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
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.*;


public abstract class ScalarType<T, O extends Op, R extends RecOp<R, O>> {

	public static final ScalarType<RelPtr, RelOp, RelRecOp> REL_PTR =
			new ScalarType<RelPtr, RelOp, RelRecOp>(
					DataType.REL_PTR,
					RelOp.class,
					RelRecOp.class) {
				@Override
				public RelOp op(Code code, RelPtr value) {
					return value.op(null, code);
				}
				@Override
				protected void declareParameter(
						SignatureWriter<?> writer,
						Arg<RelOp> arg) {
					writer.addRelPtr(arg);
				}
				@Override
				protected RelOp arg(
						Code code,
						FuncWriter<?> writer,
						Arg<RelOp> arg) {
					return writer.relPtrArg(code, arg);
				}
			};

	private final DataType dataType;
	private final Class<? extends Op> opType;
	private final Class<? extends R> recType;

	ScalarType(
			DataType dataType,
			Class<? extends Op> opType,
			Class<? extends R> recType) {
		this.dataType = dataType;
		this.opType = opType;
		this.recType = recType;
	}

	public final DataType getDataType() {
		return this.dataType;
	}

	public final Class<? extends Op> getOpType() {
		return this.opType;
	}

	public final Class<? extends R> getRecType() {
		return this.recType;
	}

	public abstract O op(Code code, T value);

	public final Arg<O> arg(
			Signature<?> signature,
			int index,
			String name) {
		return new ScalarArg<>(signature, index, name, this);
	}

	@Override
	public String toString() {
		if (this.dataType == null) {
			return super.toString();
		}
		return this.dataType.getName();
	}

	protected abstract void declareParameter(
			SignatureWriter<?> writer,
			Arg<O> arg);

	protected abstract O arg(Code code, FuncWriter<?> writer, Arg<O> arg);

	private static final class ScalarArg<O extends Op> extends Arg<O> {

		private final ScalarType<?, O, ?> scalarType;

		ScalarArg(
				Signature<?> signature,
				int index,
				String name,
				ScalarType<?, O, ?> scalarType) {
			super(signature, index, name, scalarType.getDataType());
			this.scalarType = scalarType;
		}

		@Override
		public boolean compatibleWith(Op op) {
			return this.scalarType.getOpType().isInstance(op);
		}

		@Override
		protected void write(SignatureWriter<?> writer) {
			this.scalarType.declareParameter(writer, this);
		}

		@Override
		protected O get(Code code, FuncWriter<?> writer) {
			return this.scalarType.arg(code, writer, this);
		}

	}

}

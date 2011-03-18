/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.DataType;
import org.o42a.codegen.data.Type;


public abstract class Return<O> {

	private final Signature<?> signature;
	private final DataType dataType;

	Return(Signature<?> signature, DataType dataType) {
		this.signature = signature;
		this.dataType = dataType;
	}

	public final Signature<?> getSignature() {
		return this.signature;
	}

	public final DataType getDataType() {
		return this.dataType;
	}

	public String typeName() {
		return getDataType().getName();
	}

	@Override
	public String toString() {
		return "return " + typeName();
	}

	protected abstract O call(Code code, FuncCaller<?> caller, Op... args);

	static final class ReturnVoid extends Return<Void> {

		ReturnVoid(Signature<?> signature) {
			super(signature, DataType.VOID);
		}

		@Override
		protected Void call(Code code, FuncCaller<?> caller, Op... args) {
			caller.call(code, args);
			return null;
		}

	}

	static final class ReturnInt32 extends Return<Int32op> {

		ReturnInt32(Signature<?> signature) {
			super(signature, DataType.INT32);
		}

		@Override
		protected Int32op call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callInt32(code, args);
		}

	}

	static final class ReturnInt64 extends Return<Int64op> {

		ReturnInt64(Signature<?> signature) {
			super(signature, DataType.INT64);
		}

		@Override
		protected Int64op call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callInt64(code, args);
		}

	}

	static final class ReturnFp64 extends Return<Fp64op> {

		ReturnFp64(Signature<?> signature) {
			super(signature, DataType.FP64);
		}

		@Override
		protected Fp64op call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callFp64(code, args);
		}

	}

	static final class ReturnBool extends Return<BoolOp> {

		ReturnBool(Signature<?> signature) {
			super(signature, DataType.BOOL);
		}

		@Override
		protected BoolOp call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callBool(code, args);
		}

	}

	static final class ReturnAny extends Return<AnyOp> {

		ReturnAny(Signature<?> signature) {
			super(signature, DataType.PTR);
		}

		@Override
		protected AnyOp call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callAny(code, args);
		}

	}

	static final class ReturnData extends Return<DataOp> {

		ReturnData(Signature<?> signature) {
			super(signature, DataType.DATA_PTR);
		}

		@Override
		protected DataOp call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callData(code, args);
		}

	}

	static final class ReturnPtr<O extends StructOp> extends Return<O> {

		private final Type<O> type;

		ReturnPtr(Signature<?> signature, Type<O> type) {
			super(signature, DataType.DATA_PTR);
			this.type = type;
		}

		@Override
		public String typeName() {
			return this.type.toString();
		}

		@Override
		protected O call(Code code, FuncCaller<?> caller, Op... args) {
			return caller.callPtr(code, this.type, args);
		}

	}

}

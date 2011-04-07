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

import org.o42a.codegen.CodeId;
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

	public abstract void returnNull(Code code);

	@Override
	public String toString() {
		return "return " + typeName();
	}

	protected abstract O call(
			String name,
			Code code,
			FuncCaller<?> caller,
			Op... args);

	protected final CodeId callId(
			String name,
			Code code,
			FuncCaller<?> caller,
			Op... args) {
		if (name != null) {
			return code.nameId(name);
		}

		final int firstArg =
			code.isDebug() && getSignature().isDebuggable() ? 1 : 0;

		if (args.length <= firstArg) {
			return caller.getId().detail("result");
		}

		return caller.getId()
		.detail(args[firstArg].getId())
		.detail("return");
	}

	static final class ReturnVoid extends Return<Void> {

		ReturnVoid(Signature<?> signature) {
			super(signature, DataType.VOID);
		}

		@Override
		public void returnNull(Code code) {
			code.returnVoid();
		}

		@Override
		protected Void call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			caller.call(callId(name, code, caller, args), code, args);
			return null;
		}

	}

	static final class ReturnInt8 extends Return<Int8op> {

		ReturnInt8(Signature<?> signature) {
			super(signature, DataType.INT8);
		}

		@Override
		public void returnNull(Code code) {
			code.int8((byte) 0).returnValue(code);
		}

		@Override
		protected Int8op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt8(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnInt16 extends Return<Int16op> {

		ReturnInt16(Signature<?> signature) {
			super(signature, DataType.INT16);
		}

		@Override
		public void returnNull(Code code) {
			code.int16((short) 0).returnValue(code);
		}

		@Override
		protected Int16op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt16(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnInt32 extends Return<Int32op> {

		ReturnInt32(Signature<?> signature) {
			super(signature, DataType.INT32);
		}

		@Override
		public void returnNull(Code code) {
			code.int32(0).returnValue(code);
		}

		@Override
		protected Int32op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt32(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnInt64 extends Return<Int64op> {

		ReturnInt64(Signature<?> signature) {
			super(signature, DataType.INT64);
		}

		@Override
		public void returnNull(Code code) {
			code.int64(0L).returnValue(code);
		}

		@Override
		protected Int64op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt64(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnFp32 extends Return<Fp32op> {

		ReturnFp32(Signature<?> signature) {
			super(signature, DataType.FP32);
		}

		@Override
		public void returnNull(Code code) {
			code.fp32(0.0f).returnValue(code);
		}

		@Override
		protected Fp32op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callFp32(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnFp64 extends Return<Fp64op> {

		ReturnFp64(Signature<?> signature) {
			super(signature, DataType.FP64);
		}

		@Override
		public void returnNull(Code code) {
			code.fp64(0.0d).returnValue(code);
		}

		@Override
		protected Fp64op call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callFp64(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnBool extends Return<BoolOp> {

		ReturnBool(Signature<?> signature) {
			super(signature, DataType.BOOL);
		}

		@Override
		public void returnNull(Code code) {
			code.bool(false).returnValue(code);
		}

		@Override
		protected BoolOp call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callBool(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnAny extends Return<AnyOp> {

		ReturnAny(Signature<?> signature) {
			super(signature, DataType.PTR);
		}

		@Override
		public void returnNull(Code code) {
			code.nullPtr().returnValue(code);
		}

		@Override
		protected AnyOp call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callAny(callId(name, code, caller, args), code, args);
		}

	}

	static final class ReturnData extends Return<DataOp> {

		ReturnData(Signature<?> signature) {
			super(signature, DataType.DATA_PTR);
		}

		@Override
		public void returnNull(Code code) {
			code.nullDataPtr().returnValue(code);
		}

		@Override
		protected DataOp call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callData(callId(name, code, caller, args), code, args);
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
		public void returnNull(Code code) {
			code.nullPtr(this.type).returnValue(code);
		}

		@Override
		protected O call(
				String name,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callPtr(
					callId(name, code, caller, args),
					code,
					this.type,
					args);
		}

	}

}

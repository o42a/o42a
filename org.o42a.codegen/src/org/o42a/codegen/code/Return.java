/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.util.string.ID;


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

	public abstract void returnNull(Block code);

	@Override
	public String toString() {
		return "return " + typeName();
	}

	protected abstract O call(
			ID id,
			Code code,
			FuncCaller<?> caller,
			Op... args);

	protected final ID callId(ID id, Code code, FuncCaller<?> caller) {
		if (id != null) {
			return code.opId(id);
		}
		return caller.getId().detail("result");
	}

	static final class ReturnVoid extends Return<Void> {

		ReturnVoid(Signature<?> signature) {
			super(signature, DataType.VOID);
		}

		@Override
		public void returnNull(Block code) {
			code.returnVoid();
		}

		@Override
		protected Void call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			caller.call(code, args);
			return null;
		}

	}

	static final class ReturnInt8 extends Return<Int8op> {

		ReturnInt8(Signature<?> signature) {
			super(signature, DataType.INT8);
		}

		@Override
		public void returnNull(Block code) {
			code.int8((byte) 0).returnValue(code);
		}

		@Override
		protected Int8op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt8(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnInt16 extends Return<Int16op> {

		ReturnInt16(Signature<?> signature) {
			super(signature, DataType.INT16);
		}

		@Override
		public void returnNull(Block code) {
			code.int16((short) 0).returnValue(code);
		}

		@Override
		protected Int16op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt16(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnInt32 extends Return<Int32op> {

		ReturnInt32(Signature<?> signature) {
			super(signature, DataType.INT32);
		}

		@Override
		public void returnNull(Block code) {
			code.int32(0).returnValue(code);
		}

		@Override
		protected Int32op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt32(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnInt64 extends Return<Int64op> {

		ReturnInt64(Signature<?> signature) {
			super(signature, DataType.INT64);
		}

		@Override
		public void returnNull(Block code) {
			code.int64(0L).returnValue(code);
		}

		@Override
		protected Int64op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callInt64(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnFp32 extends Return<Fp32op> {

		ReturnFp32(Signature<?> signature) {
			super(signature, DataType.FP32);
		}

		@Override
		public void returnNull(Block code) {
			code.fp32(0.0f).returnValue(code);
		}

		@Override
		protected Fp32op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callFp32(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnFp64 extends Return<Fp64op> {

		ReturnFp64(Signature<?> signature) {
			super(signature, DataType.FP64);
		}

		@Override
		public void returnNull(Block code) {
			code.fp64(0.0d).returnValue(code);
		}

		@Override
		protected Fp64op call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callFp64(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnBool extends Return<BoolOp> {

		ReturnBool(Signature<?> signature) {
			super(signature, DataType.BOOL);
		}

		@Override
		public void returnNull(Block code) {
			code.bool(false).returnValue(code);
		}

		@Override
		protected BoolOp call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callBool(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnAny extends Return<AnyOp> {

		ReturnAny(Signature<?> signature) {
			super(signature, DataType.PTR);
		}

		@Override
		public void returnNull(Block code) {
			code.nullPtr().returnValue(code);
		}

		@Override
		protected AnyOp call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callAny(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnData extends Return<DataOp> {

		ReturnData(Signature<?> signature) {
			super(signature, DataType.DATA_PTR);
		}

		@Override
		public void returnNull(Block code) {
			code.nullDataPtr().returnValue(code);
		}

		@Override
		protected DataOp call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callData(
					callId(id, code, caller),
					code,
					args);
		}

	}

	static final class ReturnPtr<S extends StructOp<S>> extends Return<S> {

		private final Type<S> type;

		ReturnPtr(Signature<?> signature, Type<S> type) {
			super(signature, DataType.DATA_PTR);
			this.type = type;
		}

		@Override
		public String typeName() {
			return this.type.toString();
		}

		@Override
		public void returnNull(Block code) {
			code.nullPtr(this.type).returnValue(code);
		}

		@Override
		protected S call(
				ID id,
				Code code,
				FuncCaller<?> caller,
				Op... args) {
			return caller.callPtr(
					callId(id, code, caller),
					code,
					this.type,
					args);
		}

	}

}

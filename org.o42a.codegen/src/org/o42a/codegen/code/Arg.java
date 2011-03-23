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

import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.DataType;
import org.o42a.codegen.data.Type;


public abstract class Arg<O extends Op> {

	private final Signature<?> signature;
	private final int index;
	private final String name;
	private final DataType dataType;

	Arg(Signature<?> signature, int index, String name, DataType dataType) {
		this.signature = signature;
		this.index = index;
		this.name = name;
		this.dataType = dataType;
	}

	public final Signature<?> getSignature() {
		return this.signature;
	}

	public final int getIndex() {
		return this.index;
	}

	public final String getName() {
		return this.name;
	}

	public final DataType getDataType() {
		return this.dataType;
	}

	public String typeName() {
		return getDataType().getName();
	}

	public abstract boolean compatibleWith(Op op);

	protected abstract O get(Code code, FuncWriter<?> writer);

	@Override
	public String toString() {

		final String name = getName();

		if (name != null) {
			return typeName() + ' ' + name;
		}

		return typeName() + " #" + getIndex();
	}

	static final class Int8arg extends Arg<Int8op> {

		Int8arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.INT8);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Int8op;
		}

		@Override
		protected Int8op get(Code code, FuncWriter<?> writer) {
			return writer.int8arg(code, getIndex());
		}

	}

	static final class Int16arg extends Arg<Int16op> {

		Int16arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.INT16);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Int16op;
		}

		@Override
		protected Int16op get(Code code, FuncWriter<?> writer) {
			return writer.int16arg(code, getIndex());
		}

	}

	static final class Int32arg extends Arg<Int32op> {

		Int32arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.INT32);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Int32op;
		}

		@Override
		protected Int32op get(Code code, FuncWriter<?> writer) {
			return writer.int32arg(code, getIndex());
		}

	}

	static final class Int64arg extends Arg<Int64op> {

		Int64arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.INT64);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Int64op;
		}

		@Override
		protected Int64op get(Code code, FuncWriter<?> writer) {
			return writer.int64arg(code, getIndex());
		}

	}

	static final class Fp32arg extends Arg<Fp32op> {

		Fp32arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.FP32);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Fp64op;
		}

		@Override
		protected Fp32op get(Code code, FuncWriter<?> writer) {
			return writer.fp32arg(code, getIndex());
		}

	}

	static final class Fp64arg extends Arg<Fp64op> {

		Fp64arg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.FP64);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof Fp64op;
		}

		@Override
		protected Fp64op get(Code code, FuncWriter<?> writer) {
			return writer.fp64arg(code, getIndex());
		}

	}

	static final class BoolArg extends Arg<BoolOp> {

		BoolArg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.BOOL);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof BoolOp;
		}

		@Override
		protected BoolOp get(Code code, FuncWriter<?> writer) {
			return writer.boolArg(code, getIndex());
		}

	}

	static final class RelPtrArg extends Arg<RelOp> {

		RelPtrArg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.REL_PTR);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof RelOp;
		}

		@Override
		protected RelOp get(Code code, FuncWriter<?> writer) {
			return writer.relPtrArg(code, getIndex());
		}

	}

	static final class AnyArg extends Arg<AnyOp> {

		AnyArg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.REL_PTR);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof AnyOp;
		}

		@Override
		protected AnyOp get(Code code, FuncWriter<?> writer) {
			return writer.ptrArg(code, getIndex());
		}

	}

	static final class DataArg extends Arg<DataOp> {

		DataArg(Signature<?> signature, int index, String name) {
			super(signature, index, name, DataType.DATA_PTR);
		}

		@Override
		public boolean compatibleWith(Op op) {
			return op instanceof DataOp;
		}

		@Override
		protected DataOp get(Code code, FuncWriter<?> writer) {
			return writer.dataArg(code, getIndex());
		}

	}

	static final class PtrArg<O extends StructOp> extends Arg<O> {

		private final Type<O> type;

		PtrArg(
				Signature<?> signature,
				int index,
				String name,
				Type<O> type) {
			super(signature, index, name, DataType.DATA_PTR);
			this.type = type;
		}

		@Override
		public String typeName() {
			return this.type.toString();
		}

		@Override
		public boolean compatibleWith(Op op) {
			if (!(op instanceof StructOp)) {
				return false;
			}

			final StructOp struct = (StructOp) op;

			return struct.getType().getType() == this.type.getType();
		}

		@Override
		protected O get(Code code, FuncWriter<?> writer) {
			return writer.ptrArg(code, getIndex(), this.type);
		}

	}

	static final class FuncPtrArg<F extends Func> extends Arg<F> {

		private final Signature<F> targetSignature;

		FuncPtrArg(
				Signature<?> signature,
				int index,
				String name,
				Signature<F> targetSignature) {
			super(signature, index, name, DataType.DATA_PTR);
			this.targetSignature = targetSignature;
		}

		@Override
		public String typeName() {
			return this.targetSignature.toString() + '*';
		}

		@Override
		public boolean compatibleWith(Op op) {
			if (!(op instanceof Func)) {
				return false;
			}

			final Func func = (Func) op;

			return func.getSignature() == this.targetSignature;
		}

		@Override
		protected F get(Code code, FuncWriter<?> writer) {
			return writer.funcPtrArg(code, getIndex(), this.targetSignature);
		}

	}

}

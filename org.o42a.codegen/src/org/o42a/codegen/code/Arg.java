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

import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.DataType;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public abstract class Arg<O extends Op> {

	private final Signature<?> signature;
	private final int index;
	private final String name;
	private final ID id;
	private final DataType dataType;

	protected Arg(
			Signature<?> signature,
			int index,
			String name,
			DataType dataType) {
		this.signature = signature;
		this.index = index;
		this.name = name;
		this.id = ID.id(name);
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

	public final ID getId() {
		return this.id;
	}

	public final DataType getDataType() {
		return this.dataType;
	}

	public String typeName() {
		return getDataType().getName();
	}

	public abstract boolean compatibleWith(Op op);

	protected abstract void write(SignatureWriter<?> writer);

	protected abstract O get(Code code, FuncWriter<?> writer);

	@Override
	public String toString() {

		final String name = getName();

		if (name != null) {
			return typeName() + ' ' + name;
		}

		return typeName() + " #" + getIndex();
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
		protected void write(SignatureWriter<?> writer) {
			writer.addBool(this);
		}

		@Override
		protected BoolOp get(Code code, FuncWriter<?> writer) {
			return writer.boolArg(code, this);
		}

	}

	static final class PtrArg<S extends StructOp<S>> extends Arg<S> {

		private final Type<S> type;

		PtrArg(
				Signature<?> signature,
				int index,
				String name,
				Type<S> type) {
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

			final StructOp<?> struct = (StructOp<?>) op;

			return struct.getType().getType() == this.type.getType();
		}

		@Override
		protected void write(SignatureWriter<?> writer) {
			writer.addPtr(this, this.type);
		}

		@Override
		protected S get(Code code, FuncWriter<?> writer) {
			return writer.ptrArg(code, this, this.type);
		}

	}

	static final class FuncPtrArg<F extends Func<F>> extends Arg<F> {

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

			final Func<?> func = (Func<?>) op;

			return func.getSignature() == this.targetSignature;
		}

		@Override
		protected void write(SignatureWriter<?> writer) {
			writer.addFuncPtr(this, this.targetSignature);
		}

		@Override
		protected F get(Code code, FuncWriter<?> writer) {
			return writer.funcPtrArg(code, this, this.targetSignature);
		}

	}

}

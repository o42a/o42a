/*
    Compiler Core
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
package org.o42a.core.ir.value;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.util.string.ID;


public final class ValAllocFunc extends Func<ValAllocFunc> {

	public static final Signature VAL_ALLOC = new Signature();

	private ValAllocFunc(FuncCaller<ValAllocFunc> caller) {
		super(caller);
	}

	public final AnyOp allocate(ValDirs dirs, int size) {
		return allocate(dirs, dirs.code().int32(size));
	}

	public final AnyOp allocate(ValDirs dirs, Int32op size) {

		final ValOp value = dirs.value();
		final AnyOp result = invoke(
				null,
				dirs.code(),
				VAL_ALLOC.result(),
				value.ptr(),
				size);

		value.go(dirs.code(), dirs.dirs());

		return result;
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ValAllocFunc> {

		private Return<AnyOp> result;
		private Arg<ValType.Op> value;
		private Arg<Int32op> size;

		private Signature() {
			super(ID.id("ValAllocF"));
		}

		public final Return<AnyOp> result() {
			return this.result;
		}

		public final Arg<ValType.Op> value() {
			return this.value;
		}

		public final Arg<Int32op> size() {
			return this.size;
		}

		@Override
		public ValAllocFunc op(FuncCaller<ValAllocFunc> caller) {
			return new ValAllocFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnAny();
			this.value = builder.addPtr("value", VAL_TYPE);
			this.size = builder.addInt32("size");
		}

	}

}

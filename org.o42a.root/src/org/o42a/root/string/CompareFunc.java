/*
    Root Object Definition
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
package org.o42a.root.string;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType.Op;
import org.o42a.util.string.ID;


public class CompareFunc extends Func<CompareFunc> {

	public static final Signature COMPARE = new Signature();

	private CompareFunc(FuncCaller<CompareFunc> caller) {
		super(caller);
	}

	public Int64op compare(Code code, ValOp what, ValOp with) {
		return invoke(null, code, COMPARE.result(), what.ptr(), with.ptr());
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<CompareFunc> {

		private Return<Int64op> result;
		private Arg<Op> what;
		private Arg<Op> with;

		private Signature() {
			super(ID.id("CompareF"));
		}

		public final Return<Int64op> result() {
			return this.result;
		}

		public final Arg<Op> what() {
			return this.what;
		}

		public final Arg<Op> with() {
			return this.with;
		}

		@Override
		public CompareFunc op(FuncCaller<CompareFunc> caller) {
			return new CompareFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnInt64();
			this.what = builder.addPtr("what", VAL_TYPE);
			this.with = builder.addPtr("with", VAL_TYPE);
		}

	}

}

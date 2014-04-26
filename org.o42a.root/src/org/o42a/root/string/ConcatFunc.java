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
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public class ConcatFunc extends Func<ConcatFunc> {

	public static final Signature CONCAT = new Signature();

	private ConcatFunc(FuncCaller<ConcatFunc> caller) {
		super(caller);
	}

	public void concat(Code code, ValOp output, ValOp what, ValOp with) {
		invoke(
				null,
				code,
				CONCAT.result(),
				output.ptr(code),
				what.ptr(code),
				with.ptr(code));
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ConcatFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> output;
		private Arg<ValType.Op> what;
		private Arg<ValType.Op> with;

		private Signature() {
			super(ID.id("ConcatF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> output() {
			return this.output;
		}

		public final Arg<ValType.Op> what() {
			return this.what;
		}

		public final Arg<ValType.Op> with() {
			return this.with;
		}

		@Override
		public ConcatFunc op(FuncCaller<ConcatFunc> caller) {
			return new ConcatFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.output = builder.addPtr("output", VAL_TYPE);
			this.what = builder.addPtr("what", VAL_TYPE);
			this.with = builder.addPtr("with", VAL_TYPE);
		}

	}

}

/*
    Intrinsics
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.intrinsic.numeric;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public class Fp64ToStringFunc extends Func<Fp64ToStringFunc> {

	public static final Fp64ToString FP64_TO_STRING = new Fp64ToString();

	private Fp64ToStringFunc(FuncCaller<Fp64ToStringFunc> caller) {
		super(caller);
	}

	public ValOp convert(ValDirs stringDirs, Fp64op value) {

		final Block code = stringDirs.code();
		final ValOp string = stringDirs.value();

		invoke(
				null,
				code,
				FP64_TO_STRING.result(),
				string.ptr(),
				value);

		return string;
	}

	public static final class Fp64ToString
			extends Signature<Fp64ToStringFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> string;
		private Arg<Fp64op> value;

		private Fp64ToString() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> string() {
			return this.string;
		}

		public final Arg<Fp64op> value() {
			return this.value;
		}

		@Override
		public Fp64ToStringFunc op(FuncCaller<Fp64ToStringFunc> caller) {
			return new Fp64ToStringFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("Fp64toStringF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.string = builder.addPtr("string", ValType.VAL_TYPE);
			this.value = builder.addFp64("value");
		}

	}

}

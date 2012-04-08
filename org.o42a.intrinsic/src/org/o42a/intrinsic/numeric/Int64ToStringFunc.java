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
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public class Int64ToStringFunc extends Func<Int64ToStringFunc> {

	public static final Int64ToString INT64_TO_STRING = new Int64ToString();

	private Int64ToStringFunc(FuncCaller<Int64ToStringFunc> caller) {
		super(caller);
	}

	public ValOp convert(ValDirs stringDirs, Int64op value) {

		final Block code = stringDirs.code();
		final ValOp string = stringDirs.value();

		invoke(
				null,
				code,
				INT64_TO_STRING.result(),
				string.ptr(),
				value);

		string.go(code, stringDirs);

		return string;
	}

	public static final class Int64ToString
			extends Signature<Int64ToStringFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> string;
		private Arg<Int64op> value;

		private Int64ToString() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> string() {
			return this.string;
		}

		public final Arg<Int64op> value() {
			return this.value;
		}

		@Override
		public Int64ToStringFunc op(FuncCaller<Int64ToStringFunc> caller) {
			return new Int64ToStringFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("Int64toStringF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.string = builder.addPtr("string", ValType.VAL_TYPE);
			this.value = builder.addInt64("value");
		}

	}

}

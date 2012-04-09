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
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public class IntegerStringFunc extends Func<IntegerStringFunc> {

	public static final IntegerString INTEGER_STRING = new IntegerString();

	private IntegerStringFunc(FuncCaller<IntegerStringFunc> caller) {
		super(caller);
	}

	public ValOp convert(ValDirs stringDirs, Int64op value) {

		final Block code = stringDirs.code();
		final ValOp string = stringDirs.value();
		final BoolOp result = invoke(
				null,
				code,
				INTEGER_STRING.result(),
				string.ptr(),
				value);

		result.goUnless(code, stringDirs.falseDir());

		return string;
	}

	public static final class IntegerString
			extends Signature<IntegerStringFunc> {

		private Return<BoolOp> result;
		private Arg<ValType.Op> string;
		private Arg<Int64op> value;

		private IntegerString() {
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<ValType.Op> string() {
			return this.string;
		}

		public final Arg<Int64op> value() {
			return this.value;
		}

		@Override
		public IntegerStringFunc op(FuncCaller<IntegerStringFunc> caller) {
			return new IntegerStringFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("IntegerStringF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.string = builder.addPtr("string", ValType.VAL_TYPE);
			this.value = builder.addInt64("value");
		}

	}

}

/*
    Root Object Definition
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.root.numeric;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public class FloatStringFunc extends Func<FloatStringFunc> {

	public static final Signature FLOAT_TO_STRING = new Signature();

	private FloatStringFunc(FuncCaller<FloatStringFunc> caller) {
		super(caller);
	}

	public ValOp convert(ValDirs stringDirs, Fp64op value) {

		final Block code = stringDirs.code();
		final ValOp string = stringDirs.value();
		final BoolOp result = invoke(
				null,
				code,
				FLOAT_TO_STRING.result(),
				string.ptr(),
				value);

		result.goUnless(code, stringDirs.falseDir());

		return string;
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<FloatStringFunc> {

		private Return<BoolOp> result;
		private Arg<ValType.Op> string;
		private Arg<Fp64op> value;

		private Signature() {
			super(ID.id("FloatStringF"));
		}

		public final Return<BoolOp> result() {
			return this.result;
		}

		public final Arg<ValType.Op> string() {
			return this.string;
		}

		public final Arg<Fp64op> value() {
			return this.value;
		}

		@Override
		public FloatStringFunc op(FuncCaller<FloatStringFunc> caller) {
			return new FloatStringFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnBool();
			this.string = builder.addPtr("string", ValType.VAL_TYPE);
			this.value = builder.addFp64("value");
		}

	}

}

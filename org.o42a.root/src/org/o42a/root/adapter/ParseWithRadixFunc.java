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
package org.o42a.root.adapter;

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.util.string.ID;


public final class ParseWithRadixFunc extends Func<ParseWithRadixFunc> {

	public static final Signature PARSE_WITH_RADIX = new Signature();

	private ParseWithRadixFunc(FuncCaller<ParseWithRadixFunc> caller) {
		super(caller);
	}

	public ValOp parse(ValDirs dirs, ValOp input, int radix) {

		final Block code = dirs.code();
		final ValOp output = dirs.value();

		parse(code, output, input, radix);
		output.go(code, dirs);

		return output;
	}

	public void parse(Code code, ValOp output, ValOp input, int radix) {
		invoke(
				null,
				code,
				PARSE_WITH_RADIX.result(),
				output.ptr(),
				input.ptr(),
				code.int32(radix));
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<ParseWithRadixFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> output;
		private Arg<ValType.Op> input;
		private Arg<Int32op> radix;

		private Signature() {
			super(ID.id("ParseWithRadixF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> output() {
			return this.output;
		}

		public final Arg<ValType.Op> input() {
			return this.input;
		}

		public final Arg<Int32op> radix() {
			return this.radix;
		}

		@Override
		public ParseWithRadixFunc op(FuncCaller<ParseWithRadixFunc> caller) {
			return new ParseWithRadixFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.output = builder.addPtr("output", VAL_TYPE);
			this.input = builder.addPtr("input", VAL_TYPE);
			this.radix = builder.addInt32("radix");
		}

	}

}

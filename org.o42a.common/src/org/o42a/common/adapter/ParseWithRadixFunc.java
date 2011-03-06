/*
    Modules Commons
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
package org.o42a.common.adapter;

import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.core.ir.op.ValOp;


public final class ParseWithRadixFunc extends Func {

	public static final Signature<ParseWithRadixFunc> PARSE_WITH_RADIX =
		new ParseWithRadix();

	private ParseWithRadixFunc(FuncCaller caller) {
		super(caller);
	}

	public void parse(Code code, ValOp result, ValOp input, int radix) {
		caller().call(code, result, input, code.int32(radix));
	}

	private static final class ParseWithRadix
			extends Signature<ParseWithRadixFunc> {

		private ParseWithRadix() {
			super("void", "ParseWithRadixF", "Val*, Val*, int32");
		}

		@Override
		public ParseWithRadixFunc op(FuncCaller caller) {
			return new ParseWithRadixFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ParseWithRadixFunc> writer) {
			writer.returnVoid();
			writer.addPtr(VAL_TYPE);
			writer.addPtr(VAL_TYPE);
			writer.addInt32();
		}

	}

}

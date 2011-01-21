/*
    Intrinsics
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
package org.o42a.intrinsic.numeric;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.ValOp;


class ParseFunc extends Func {

	private static FuncSignature signature;

	public static Signature<ParseFunc> signature(
			IRGenerator generator) {
		if (signature != null && signature.generator == generator) {
			return signature;
		}
		return signature = new FuncSignature(generator);
	}

	private ParseFunc(FuncCaller caller) {
		super(caller);
	}

	public void parse(Code code, ValOp result, ValOp input) {
		caller().call(code, result, input);
	}

	public static final class FuncSignature
			extends Signature<ParseFunc> {

		private final IRGenerator generator;

		private FuncSignature(IRGenerator generator) {
			super("void", "ParseF", "Val*, Val*");
			this.generator = generator;
		}

		@Override
		public ParseFunc op(FuncCaller caller) {
			return new ParseFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ParseFunc> writer) {
			writer.returnVoid();
			writer.addPtr(this.generator.valType());
			writer.addPtr(this.generator.valType());
		}

	}

}

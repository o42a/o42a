/*
    Console Module
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.lib.console.impl;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.op.ValOp;


public final class PrintFunc extends Func {

	private static PrintSignature signature;

	public static Signature<PrintFunc> printSignature(IRGenerator generator) {
		if (signature != null && signature.generator == generator) {
			return signature;
		}
		return signature = new PrintSignature(generator);
	}

	private PrintFunc(FuncCaller caller) {
		super(caller);
	}

	public void print(Code code, ValOp text) {
		caller().call(code, text);
	}

	public static final class PrintSignature extends Signature<PrintFunc> {

		private final IRGenerator generator;

		private PrintSignature(IRGenerator generator) {
			super("void", "console.PrintF", "Val*");
			this.generator = generator;
		}

		@Override
		public PrintFunc op(FuncCaller caller) {
			return new PrintFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<PrintFunc> writer) {
			writer.returnVoid();
			writer.addPtr(this.generator.valType());
		}

	}

}

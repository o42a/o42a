/*
    Console Module
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.value.ValType.VAL_TYPE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;


public final class PrintFunc extends Func<PrintFunc> {

	public static final Print PRINT = new Print();

	private PrintFunc(FuncCaller<PrintFunc> caller) {
		super(caller);
	}

	public void print(Code code, ValOp text) {
		invoke(null, code, PRINT.result(), text.ptr());
	}

	public static final class Print extends Signature<PrintFunc> {

		private Return<Void> result;
		private Arg<ValType.Op> text;

		private Print() {
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<ValType.Op> text() {
			return this.text;
		}

		@Override
		public PrintFunc op(FuncCaller<PrintFunc> caller) {
			return new PrintFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("console").sub("PrintF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.text = builder.addPtr("text", VAL_TYPE);
		}

	}

}

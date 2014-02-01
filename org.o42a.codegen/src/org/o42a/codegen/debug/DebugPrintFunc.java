/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.Debug.DEBUG_ID;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;


public final class DebugPrintFunc extends Func<DebugPrintFunc> {

	public static final Signature DEBUG_PRINT = new Signature();

	private DebugPrintFunc(FuncCaller<DebugPrintFunc> caller) {
		super(caller);
	}

	public void call(Code code, AnyOp message) {
		invoke(null, code, DEBUG_PRINT.result(), message);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<DebugPrintFunc> {

		private Return<Void> result;
		private Arg<AnyOp> message;

		private Signature() {
			super(DEBUG_ID.sub("PrintF"));
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<AnyOp> message() {
			return this.message;
		}

		@Override
		public DebugPrintFunc op(FuncCaller<DebugPrintFunc> caller) {
			return new DebugPrintFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.message = builder.addPtr("message");
		}

	}

}

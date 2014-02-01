/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import static org.o42a.codegen.debug.Debug.DEBUG_ID;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.Int32op;


final class DebugDoneFunc extends Func<DebugDoneFunc> {

	static final Signature DEBUG_DONE = new Signature();

	private DebugDoneFunc(FuncCaller<DebugDoneFunc> caller) {
		super(caller);
	}

	public final void call(Code code) {
		invoke(null, code, DEBUG_DONE.result(), code.int32(0));
	}

	static final class Signature
			extends org.o42a.codegen.code.Signature<DebugDoneFunc> {

		private Return<Void> result;
		private Arg<Int32op> line;

		private Signature() {
			super(DEBUG_ID.sub("DoneF"));
		}

		@Override
		public boolean isDebuggable() {
			return false;
		}

		public final Return<Void> result() {
			return this.result;
		}

		public final Arg<Int32op> line() {
			return this.line;
		}

		@Override
		public DebugDoneFunc op(FuncCaller<DebugDoneFunc> caller) {
			return new DebugDoneFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnVoid();
			this.line = builder.addInt32("line");
		}

	}

}

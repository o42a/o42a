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
package org.o42a.lib.console;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class DebuggableMainFunc extends Func<DebuggableMainFunc> {

	public static final DebuggableMain DEBUGGABLE_MAIN = new DebuggableMain();

	private DebuggableMainFunc(FuncCaller<DebuggableMainFunc> caller) {
		super(caller);
	}

	public Int32op call(Code code, Int32op argc, AnyOp argv) {
		return invoke(null, code, DEBUGGABLE_MAIN.result(), argc, argv);
	}

	public static final class DebuggableMain
			extends Signature<DebuggableMainFunc> {

		private Return<Int32op> result;
		private Arg<Int32op> argc;
		private Arg<AnyOp> argv;

		private DebuggableMain() {
		}

		public final Return<Int32op> result() {
			return this.result;
		}

		public final Arg<Int32op> argc() {
			return this.argc;
		}

		public final Arg<AnyOp> argv() {
			return this.argv;
		}

		@Override
		public DebuggableMainFunc op(FuncCaller<DebuggableMainFunc> caller) {
			return new DebuggableMainFunc(caller);
		}

		@Override
		protected CodeId buildCodeId(CodeIdFactory factory) {
			return factory.id("console").sub("MainF");
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnInt32();
			this.argc = builder.addInt32("argc");
			this.argv = builder.addPtr("argv");
		}

	}

}

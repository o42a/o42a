/*
    Console Module
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
package org.o42a.lib.console;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;


public final class MainFunc extends Func<MainFunc> {

	public static final Signature MAIN = new Signature();

	private MainFunc(FuncCaller<MainFunc> caller) {
		super(caller);
	}

	public Int32op call(Code code, Int32op argc, AnyOp argv) {
		return invoke(null, code, MAIN.result(), argc, argv);
	}

	public static final class Signature
			extends org.o42a.codegen.code.Signature<MainFunc> {

		private Return<Int32op> result;
		private Arg<Int32op> argc;
		private Arg<AnyOp> argv;

		private Signature() {
			super(ConsoleModule.CONSOLE_ID.sub("MainF"));
		}

		@Override
		public boolean isDebuggable() {
			return false;
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
		public MainFunc op(FuncCaller<MainFunc> caller) {
			return new MainFunc(caller);
		}

		@Override
		protected void build(SignatureBuilder builder) {
			this.result = builder.returnInt32();
			this.argc = builder.addInt32("argc");
			this.argv = builder.addPtr("argv");
		}

	}

}

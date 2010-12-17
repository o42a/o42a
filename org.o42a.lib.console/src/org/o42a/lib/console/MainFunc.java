/*
    Console Module
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;


public final class MainFunc extends Func {

	public static final Signature<MainFunc> SIGNATURE = new MainSignature();

	private MainFunc(FuncCaller caller) {
		super(caller);
	}

	private static final class MainSignature extends Signature<MainFunc> {

		MainSignature() {
			super("int", "console.MainF", "int, char**");
		}

		@Override
		public MainFunc op(FuncCaller caller) {
			return new MainFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<MainFunc> writer) {
			writer.returnInt32();
			writer.addInt32();
			writer.addAny();
		}

	}

}

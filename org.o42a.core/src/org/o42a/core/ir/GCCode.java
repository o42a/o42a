/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.ir;

import static org.o42a.core.ir.op.NoArgFunc.NO_ARG;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Disposal;


public final class GCCode {

	private static final SignalGC SIGNAL_GC = new SignalGC();

	private final CodeBuilder builder;
	private boolean signalSent;

	GCCode(CodeBuilder builder) {
		this.builder = builder;
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public final void signal() {
		if (this.signalSent) {
			return;
		}
		this.signalSent = true;
		getBuilder().getFunction().addLastDisposal(SIGNAL_GC);
	}

	private static final class SignalGC implements Disposal {

		@Override
		public void dispose(Code code) {
			code.getGenerator()
			.externalFunction()
			.link("o42a_gc_signal", NO_ARG)
			.op(null, code)
			.call(code);
		}

		@Override
		public String toString() {
			return "SignalGC";
		}

	}

}

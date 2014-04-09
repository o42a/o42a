/*
    Compiler Core
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
package org.o42a.core.ir;

import static org.o42a.codegen.code.AllocationMode.NO_ALLOCATION;
import static org.o42a.core.ir.op.NoArgFunc.NO_ARG;

import org.o42a.codegen.code.*;
import org.o42a.util.string.ID;


public final class GCCode {

	private static final ID SIGNAL_GC_ID = ID.id("__signal_gc__");
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
		getBuilder().getFunction().allocate(SIGNAL_GC_ID, SIGNAL_GC);
	}

	private static final class SignalGC implements Allocatable<Void> {

		@Override
		public AllocationMode getAllocationMode() {
			return NO_ALLOCATION;
		}

		@Override
		public int getDisposePriority() {
			return DEBUG_DISPOSE_PRIORITY;
		}

		@Override
		public Void allocate(Allocations code, Allocated<Void> allocated) {
			return null;
		}

		@Override
		public void init(Code code, Allocated<Void> allocated) {
		}

		@Override
		public void dispose(Code code, Allocated<Void> allocated) {
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

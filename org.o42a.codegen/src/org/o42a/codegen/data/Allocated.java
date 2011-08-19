/*
    Compiler Code Generator
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
package org.o42a.codegen.data;

import org.o42a.codegen.code.op.StructOp;


public class Allocated<S extends StructOp<S>, C> {

	private final C container;
	private final Type<S> type;
	private final SubData<S> data;

	Allocated(C container, Type<S> type, SubData<S> data) {
		this.container = container;
		this.type = type;
		this.data = data;
	}

	public final C getContainer() {
		return this.container;
	}

	public final Type<S> getType() {
		return this.type;
	}

	public final SubData<S> getData() {
		return this.data;
	}

	public void done() {
		this.data.endAllocation();
	}

	static final class AllocatedGlobal<
			S extends StructOp<S>,
			T extends Type<S>> extends Allocated<S, Global<S, T>> {

		public AllocatedGlobal(
				Global<S, T> global,
				Type<S> type,
				SubData<S> data) {
			super(global, type, data);
		}

		@Override
		public void done() {
			super.done();

			final Global<S, T> global = getContainer();

			global.getGenerator().getGlobals().globalAllocated(global);
		}

	}

}

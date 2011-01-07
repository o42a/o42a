/*
    Compiler Code Generator
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
package org.o42a.codegen.debug;

import java.util.ArrayList;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Data;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;


final class DbgGlobals extends Struct<DbgGlobals.Op> {

	private final Debug debug;
	private final ArrayList<Data<?>> globals = new ArrayList<Data<?>>();

	DbgGlobals(Debug debug) {
		super("DEBUG.GLOBALS");
		this.debug = debug;
	}

	public final int numGlobals() {
		return this.globals.size();
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected void allocate(SubData<Op> data) {
		for (Data<?> global : this.globals) {
			data.addInstance(
					global.getId(),
					this.debug.dbgGlobalType(),
					new DbgGlobal(global));
		}
	}

	@Override
	protected void fill() {
	}

	void addGlobal(Data<?> global) {
		this.globals.add(global);
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

}

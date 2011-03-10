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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.*;


final class DebugInfo extends Struct<DebugInfo.Op> {

	private final DbgFunctions functions;
	private final DbgGlobals globals;

	DebugInfo() {
		this.functions = new DbgFunctions();
		this.globals = new DbgGlobals();
	}

	@Override
	public boolean isDebugInfo() {
		return true;
	}

	public final DbgFunctions functions() {
		return this.functions;
	}

	public final DbgGlobals globals() {
		return this.globals;
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.rawId("o42a_debug_info");
	}

	@Override
	protected void allocate(SubData<Op> data) {

		final Generator generator = data.getGenerator();
		final DbgFunctions functions =
			generator.newGlobal().setConstant().create(this.functions)
			.getInstance();
		final DbgGlobals globals =
			generator.newGlobal().setConstant().create(this.globals)
			.getInstance();

		final StructPtrRec<DbgFunctions.Op> functionsPtr =
			data.addPtr("functions", functions);
		final StructPtrRec<DbgGlobals.Op> globalsPtr =
			data.addPtr("globals", globals);
		final Rec<DataOp<Int32op>, Integer> numFunctions =
			data.addInt32("num_functions");
		final Rec<DataOp<Int32op>, Integer> numGlobals =
			data.addInt32("num_globals");

		functionsPtr.setValue(functions.data(generator).getPointer());
		globalsPtr.setValue(globals.data(generator).getPointer());
		numFunctions.setValue(this.functions.getNumFunctions());
		numGlobals.setValue(this.globals.getNumGlobals());
	}

	@Override
	protected void fill() {
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}

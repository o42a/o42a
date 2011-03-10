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

import static org.o42a.codegen.debug.DbgFuncType.DBG_FUNC_TYPE;

import java.util.ArrayList;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.CodePtr;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;


final class DbgFunctions extends Struct<DbgFunctions.Op> {

	private final ArrayList<DbgFunc> functions = new ArrayList<DbgFunc>();

	@Override
	public boolean isDebugInfo() {
		return true;
	}

	public final int getNumFunctions() {
		return this.functions.size();
	}

	@Override
	public Op op(StructWriter writer) {
		return new Op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return factory.id("DEBUG").sub("FUNCTIONS");
	}

	@Override
	protected void allocate(SubData<Op> data) {
		for (DbgFunc func : this.functions) {
			data.addInstance(func.getId(), DBG_FUNC_TYPE, func);
		}
	}

	@Override
	protected void fill() {
	}

	<F extends Func> DbgFunc addFunction(
			CodeId id,
			Signature<F> signature,
			CodePtr<F> function) {

		final DbgFunc dbgFunc = new DbgFunc(id, signature, function);

		this.functions.add(dbgFunc);

		return dbgFunc;
	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

	}

}

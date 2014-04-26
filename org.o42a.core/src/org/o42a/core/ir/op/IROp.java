/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DumpablePtrOp;
import org.o42a.codegen.debug.Dumpable;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


public abstract class IROp implements Dumpable {

	private final CodeBuilder builder;

	public IROp(CodeBuilder builder) {
		this.builder = builder;
	}

	public final CompilerContext getContext() {
		return this.builder.getContext();
	}

	public final Generator getGenerator() {
		return this.builder.getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public abstract DumpablePtrOp<?> ptr(Code code);

	@Override
	public final DataOp toData(ID id, Code code) {
		return ptr(code).toData(id, code);
	}

	@Override
	public final AnyOp toAny(ID id, Code code) {
		return ptr(code).toAny(null, code);
	}

}

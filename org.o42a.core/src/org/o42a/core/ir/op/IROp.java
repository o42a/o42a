/*
    Compiler Core
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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.core.CompilerContext;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.IRGenerator;


public abstract class IROp {

	final PtrOp ptr;
	private final CodeBuilder builder;

	public IROp(CodeBuilder builder, PtrOp ptr) {
		this.builder = builder;
		this.ptr = ptr;
	}

	public final CompilerContext getContext() {
		return this.builder.getContext();
	}

	public final IRGenerator getGenerator() {
		return this.builder.getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public PtrOp ptr() {
		return this.ptr;
	}

	public final AnyOp toAny(Code code) {
		return this.ptr.toAny(code);
	}

}

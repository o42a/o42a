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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DumpablePtrOp;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.util.string.ID;


public abstract class DefiniteIROp<P extends DumpablePtrOp<P>>
		extends IROp<P>
		implements OpMeans<P> {

	private final OpMeans<P> ptr;

	public DefiniteIROp(CodeBuilder builder, OpMeans<P> ptr) {
		super(builder);
		this.ptr = ptr;
	}

	@Override
	public ID getId() {
		return ptr().getId();
	}

	public final P ptr() {
		return op();
	}

	@Override
	public final P op() {
		return this.ptr.op();
	}

	@Override
	public final P ptr(Code code) {
		return op();
	}

}

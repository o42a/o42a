/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.backend.constant.data.struct;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.CodeIdFactory;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public final class CType<S extends StructOp<S>> extends Struct<S> {

	private final Type<S> original;

	public CType(Type<S> original) {
		this.original = original;
	}

	@Override
	public boolean isPacked() {
		return this.original.isPacked();
	}

	@Override
	public boolean isReentrant() {
		return false;
	}

	@Override
	public boolean isDebugInfo() {
		return false;
	}

	@Override
	public boolean isDebuggable() {
		return false;
	}

	public final Type<S> getOriginal() {
		return this.original;
	}

	@Override
	public S op(StructWriter<S> writer) {
		return getOriginal().op(writer);
	}

	@Override
	protected CodeId buildCodeId(CodeIdFactory factory) {
		return getOriginal().codeId(factory);
	}

	@Override
	protected void allocate(SubData<S> data) {
		throw new IllegalStateException(
				"Type should be manually constructed: " + this);
	}

	@Override
	protected void fill() {
	}

}

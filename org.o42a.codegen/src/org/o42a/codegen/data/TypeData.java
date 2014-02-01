/*
    Compiler Code Generator
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


final class TypeData<S extends StructOp<S>> extends AbstractTypeData<S> {

	TypeData(Generator generator, Type<S> type) {
		super(generator, type.getId().removeLocal(), type);
	}

	@Override
	public Global<?, ?> getGlobal() {
		return null;
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@Override
	public int getDataFlags() {
		return CONSTANT;
	}

	@Override
	public S fieldOf(ID id, Code code, StructOp<?> struct) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected DataAllocation<S> startTypeAllocation(DataAllocator allocator) {
		return allocator.begin(this, getInstance());
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.end(getInstance());
	}

	@Override
	protected void write(DataWriter writer) {
		throw new UnsupportedOperationException(
				"Type " + getId() + " itself can not be written out. "
				+ "Write an instance instead.");
	}

}

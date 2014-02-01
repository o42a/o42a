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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


final class GlobalStructData<S extends StructOp<S>>
		extends AbstractTypeData<S> {

	private final Global<S, ?> global;

	GlobalStructData(Global<S, ?> global, Type<S> instance) {
		super(
				global.getGenerator(),
				global.getId().removeLocal(),
				instance);
		this.global = global;
	}

	@Override
	public final Global<S, ?> getGlobal() {
		return this.global;
	}

	@Override
	public final Type<?> getEnclosing() {
		return null;
	}

	@Override
	public final int getDataFlags() {
		return getGlobal().getDataFlags() & DATA_FLAGS;
	}

	@Override
	public S fieldOf(ID id, Code code, StructOp<?> struct) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (this.global == null) {
			return super.toString();
		}
		return this.global.toString();
	}

	@Override
	protected DataAllocation<S> startTypeAllocation(DataAllocator allocator) {
		return allocator.begin(
				this,
				getInstance().pointer(getGenerator()).getProtoAllocation(),
				getGlobal());
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.end(getGlobal());
	}

	@Override
	protected void postTypeAllocation() {
		super.postTypeAllocation();
		getGenerator().getGlobals().globalAllocated(this);
	}

	@Override
	protected void write(DataWriter writer) {
		writer.begin(getPointer().getAllocation(), this.global);
		((Struct<S>) getInstance()).fill();
		writeIncluded(writer);
		writer.end(getPointer().getAllocation(), this.global);
	}

}

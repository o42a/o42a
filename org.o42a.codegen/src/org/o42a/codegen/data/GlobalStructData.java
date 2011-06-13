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
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class GlobalStructData<O extends StructOp>
		extends AbstractTypeData<O> {

	private final Global<O, ?> global;

	GlobalStructData(Global<O, ?> global, Type<O> instance) {
		super(
				global.getGenerator(),
				global.getId().removeLocal(),
				instance);
		this.global = global;
	}

	@Override
	public Global<O, ?> getGlobal() {
		return this.global;
	}

	@Override
	public final boolean isConstant() {
		return this.global.isConstant();
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@Override
	public String toString() {
		return this.global.toString();
	}

	@Override
	protected DataAllocation<O> beginTypeAllocation(
			DataAllocator allocator) {
		return allocator.begin(getInstance().getAllocation(), this.global);
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.end(this.global);
	}

	@Override
	protected void postTypeAllocation() {
		super.postTypeAllocation();
		getGenerator().getGlobals().globalCreated(this);
	}

	@Override
	protected void write(DataWriter writer) {
		writer.begin(getPointer().getAllocation(), this.global);
		((Struct<O>) getInstance()).fill();
		writeIncluded(writer);
		writer.end(getPointer().getAllocation(), this.global);
	}

}

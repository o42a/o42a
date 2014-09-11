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


final class StructData<S extends StructOp<S>> extends AbstractTypeData<S> {

	private final Global<?, ?> global;
	private final Type<?> enclosing;
	private final boolean typeData;

	StructData(SubData<?> enclosing, Type<S> instance, ID name) {
		super(enclosing.getGenerator(), name, instance);
		this.global = enclosing.getGlobal();
		this.enclosing = enclosing.getInstance();
		this.typeData = enclosing.isTypeData();
	}

	@Override
	public final Global<?, ?> getGlobal() {
		return this.global;
	}

	@Override
	public final Type<?> getEnclosing() {
		return this.enclosing;
	}

	@Override
	public final boolean isTypeData() {
		return this.typeData;
	}

	@Override
	public final int getDataFlags() {
		return getGlobal().getDataFlags() & DATA_FLAGS;
	}

	@Override
	public S fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.struct(id, code, getInstance());
	}

	@Override
	protected DataAllocation<S> startTypeAllocation(DataAllocator allocator) {
		return allocator.enter(
				getEnclosing().getAllocation(),
				this,
				getInstance().pointer(getGenerator()).getProtoAllocation());
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.exit(getEnclosing().getAllocation(), this);
	}

	@Override
	protected void write(DataWriter writer) {
		writer.enter(getPointer().getAllocation(), this);
		if (!isTypeData()) {
			((Struct<S>) getInstance()).fill();
		}
		writeIncluded(writer);
		writer.exit(getPointer().getAllocation(), this);
	}

}

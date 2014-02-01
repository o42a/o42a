/*
    Compiler Code Generator
    Copyright (C) 2012-2014 Ruslan Lopatin

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
import org.o42a.codegen.code.op.SystemOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


public final class SystemData extends Data<SystemOp> {

	private final SubData<?> enclosing;
	private final SystemType systemType;
	private int flags;

	SystemData(SubData<?> enclosing, ID id, SystemType systemType) {
		super(enclosing.getGenerator(), id);
		this.enclosing = enclosing;
		this.systemType = systemType;
	}

	public final SystemType getSystemType() {
		return this.systemType;
	}

	@Override
	public Global<?, ?> getGlobal() {
		return this.enclosing.getGlobal();
	}

	@Override
	public Type<?> getEnclosing() {
		return this.enclosing.getInstance();
	}

	@Override
	public final Type<?> getInstance() {
		return null;
	}

	public final SystemData setConstant(boolean constant) {
		if (constant) {
			this.flags |= CONSTANT;
		} else {
			this.flags &= ~CONSTANT;
		}
		return this;
	}

	public final SystemData setAttributes(DataAttributes attributes) {
		this.flags = attributes.getDataFlags();
		return this;
	}

	@Override
	public final int getDataFlags() {
		return this.flags | (this.enclosing.getDataFlags() & NESTED_FLAGS);
	}

	@Override
	public DataType getDataType() {
		return DataType.SYSTEM;
	}

	@Override
	public SystemOp fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.system(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateSystem(
				getEnclosing().getAllocation(),
				this,
				getPointer().getProtoAllocation()));
	}

	@Override
	protected void write(DataWriter writer) {
		writer.writeSystem(getAllocation());
	}

}

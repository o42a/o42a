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
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


final class TypeInstanceData<S extends StructOp<S>>
		extends AbstractInstanceData<S> {

	private final Global<?, ?> global;
	private final Type<?> enclosing;
	private final boolean typeData;

	TypeInstanceData(
			SubData<?> enclosing,
			ID id,
			Type<S> instance,
			Content<? extends Type<S>> content) {
		super(enclosing.getGenerator(), id, instance, content);
		this.global = enclosing.getGlobal();
		this.enclosing = enclosing.getInstance();
		this.typeData = enclosing.isTypeData();
	}

	@Override
	public Global<?, ?> getGlobal() {
		return this.global;
	}

	@Override
	public Type<?> getEnclosing() {
		return this.enclosing;
	}

	@Override
	public boolean isTypeData() {
		return this.typeData;
	}

	@Override
	public int getDataFlags() {

		final Global<?, ?> global = getGlobal();

		if (global == null) {
			return 0;
		}

		return global.getDataFlags() & DATA_FLAGS;
	}

	@Override
	public S fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.struct(id, code, getInstance());
	}

	@Override
	protected void allocateType(boolean fully) {
		allocateData();
	}

	@Override
	protected boolean startAllocation(DataAllocator allocator) {
		setAllocation(allocator.enter(
				getEnclosing().getAllocation(),
				this,
				getInstance().pointer(getGenerator()).getProtoAllocation()));
		return true;
	}

	@Override
	protected void allocateContents() {
		getInstance().allocateInstance(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void endAllocation(DataAllocator allocator) {
		allocator.exit(getEnclosing().getAllocation(), this);
		this.content.allocated(getInstance());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void write(DataWriter writer) {
		writer.enter(getPointer().getAllocation(), this);
		if (!isTypeData()) {
			this.content.fill(getInstance());
		}
		writeIncluded(writer);
		writer.exit(getPointer().getAllocation(), this);
	}

}

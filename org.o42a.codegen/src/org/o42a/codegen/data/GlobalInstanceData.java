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
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class GlobalInstanceData<O extends StructOp>
		extends AbstractInstanceData<O> {

	private final Global<O, ?> global;

	GlobalInstanceData(
			Global<O, ?> global,
			Type<O> instance,
			Content<?> content) {
		super(
				global.getGenerator(),
				global.getId().removeLocal(),
				instance,
				content);
		this.global = global;
	}

	@Override
	public Global<?, ?> getGlobal() {
		return this.global;
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.begin(getAllocation(), this.global));
		getInstance().allocateInstance(this);
		allocator.end(this.global);
		this.content.allocated(getInstance());
		getGenerator().getGlobals().addGlobal(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void write(DataWriter writer) {
		writer.begin(getPointer().getAllocation(), this.global);
		this.content.fill(getInstance());
		writeIncluded(writer);
		writer.end(getPointer().getAllocation(), this.global);
		getGenerator().getGlobals().addType(this);
	}

}

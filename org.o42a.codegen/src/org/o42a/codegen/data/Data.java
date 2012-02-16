/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


public abstract class Data<P extends PtrOp<P>> implements DataAttributes {

	private final Generator generator;
	private final CodeId id;
	private Ptr<P> pointer;
	private Data<?> next;

	Data(Generator generator, CodeId id) {
		assert generator != null :
			"Code generator not specified";
		assert id != null :
			"Data identifier not specified";
		this.generator = generator;
		this.id = id;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	@Override
	public final boolean isConstant() {
		return (getDataFlags() & CONSTANT) != 0;
	}

	public abstract Global<?, ?> getGlobal();

	public abstract Type<?> getEnclosing();

	public abstract Type<?> getInstance();

	public final CodeId getId() {
		return this.id;
	}

	public abstract DataType getDataType();

	public final Ptr<P> getPointer() {
		if (this.pointer != null) {
			return this.pointer;
		}
		return this.pointer = createPointer();
	}

	public final DataLayout getLayout() {
		return getAllocation().getLayout();
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

	protected abstract void allocate(DataAllocator allocator);

	protected abstract void write(DataWriter writer);

	protected final DataAllocation<P> getAllocation() {
		return getPointer().getAllocation();
	}

	protected void setAllocation(DataAllocation<P> allocation) {
		getPointer().setAllocation(allocation);
	}

	Ptr<P> createPointer() {
		return new Ptr<P>(getId(), isConstant(), false) {
			@Override
			protected DataAllocation<P> createAllocation() {
				return null;// Will be assigned on allocation.
			}
		};
	}

	final void allocateData() {
		allocate(getGenerator().getGlobals().dataAllocator());
	}

	final Data<?> getNext() {
		return this.next;
	}

	final void setNext(Data<?> next) {
		this.next = next;
	}

}

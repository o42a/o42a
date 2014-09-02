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

import static org.o42a.codegen.data.Content.noContent;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.util.string.ID;


abstract class AbstractInstanceData<S extends StructOp<S>>
		extends SubData<S> {

	@SuppressWarnings("rawtypes")
	final Content content;
	private Data<?> next;

	AbstractInstanceData(
			Generator generator,
			ID id,
			Type<S> instance,
			Content<? extends Type<S>> content) {
		super(generator, id, instance);
		this.content = content != null ? content : noContent();
		this.next = instance.type.data.data().getFirst();
	}

	@Override
	protected <D extends Data<?>> D add(D data, boolean allocate) {
		assert this.next != null :
			"An attempt to add more fields to instance,"
			+ " than type contains: " + data + " (" + (size() + 1) + ")";
		assert data.getClass() == this.next.getClass() :
			"Wrong field " + data + " at position " + size()
			+ ", while " + this.next + " expected";

		data.getPointer().copyAllocation(this.next);
		this.next = this.next.getNext();

		return super.add(data, allocate);
	}

	@Override
	Ptr<S> createPointer() {

		final Ptr<S> pointer = super.createPointer();

		pointer.copyAllocation(getInstance().getType().getInstanceData());

		return pointer;
	}

}

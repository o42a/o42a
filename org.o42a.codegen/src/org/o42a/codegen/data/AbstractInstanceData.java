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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;


abstract class AbstractInstanceData<O extends StructOp>
		extends SubData<O> {

	@SuppressWarnings("rawtypes")
	final Content content;
	private Data<?> next;

	AbstractInstanceData(
			Generator generator,
			CodeId id,
			Type<O> instance,
			Content<?> content) {
		super(generator, id, instance);
		getPointer().copyAllocation(instance.getType().getTypeData());
		this.content = content != null ? content : Type.emptyContent();
		this.next = instance.type.data.data().getFirst();
	}

	@Override
	protected <D extends Data<?>> D add(D data) {
		assert this.next != null :
			"An attempt to add more fields to instance,"
			+ " than type contains: " + data + " (" + (size() + 1) + ")";
		assert data.getClass() == this.next.getClass() :
			"Wrong field " + data + " at position " + size()
			+ ", while " + this.next + " expected";

		data.getPointer().copyAllocation(this.next);
		this.next = this.next.getNext();

		return super.add(data);
	}

}

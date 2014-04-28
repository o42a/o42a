/*
    Compiler Code Generator
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import java.util.function.Supplier;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.data.backend.RelAllocation;
import org.o42a.util.string.ID;


public final class RelPtr implements Supplier<RelPtr> {

	private final Ptr<?> pointer;
	private final Ptr<?> relativeTo;
	private final ID id;
	private RelAllocation allocation;

	RelPtr(Ptr<?> pointer, Ptr<?> relativeTo) {
		this.pointer = pointer;
		this.relativeTo = relativeTo;
		this.id =
				this.pointer.getId()
				.detail("relative_to")
				.detail(relativeTo.getId());
	}

	@Override
	public final RelPtr get() {
		return this;
	}

	public final ID getId() {
		return this.id;
	}

	public final Ptr<?> getPointer() {
		return this.pointer;
	}

	public final Ptr<?> getRelativeTo() {
		return this.relativeTo;
	}

	public final RelAllocation getAllocation() {
		if (this.allocation != null) {
			return this.allocation;
		}
		return this.allocation = getPointer().getAllocation().relativeTo(
				this,
				getRelativeTo().getAllocation());
	}

	public final RelOp op(ID id, Code code) {

		final CodeBase c = code;

		c.assertIncomplete();

		return getAllocation().op(
				id != null ? code.opId(id) : getId(),
				c.writer());
	}

	@Override
	public String toString() {
		return this.pointer + " - " + this.relativeTo;
	}

}

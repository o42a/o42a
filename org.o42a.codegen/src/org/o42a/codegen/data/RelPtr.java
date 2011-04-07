/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.RelOp;
import org.o42a.codegen.data.backend.RelAllocation;


public final class RelPtr {

	private final Ptr<?> pointer;
	private final Ptr<?> relativeTo;
	private final CodeId id;

	RelPtr(Ptr<?> pointer, Ptr<?> relativeTo) {
		this.pointer = pointer;
		this.relativeTo = relativeTo;
		this.id =
			this.pointer.getId()
			.detail("relative_to")
			.detail(relativeTo.getId());
	}

	public final CodeId getId() {
		return this.id;
	}

	public final Ptr<?> getPointer() {
		return this.pointer;
	}

	public final Ptr<?> getRelativeTo() {
		return this.relativeTo;
	}

	public RelOp op(String name, Code code) {

		final CodeBase c = code;

		c.assertIncomplete();

		return allocation().op(
				name != null ? code.nameId(name) : getId(),
				c.writer());
	}

	@Override
	public String toString() {
		return this.pointer + " - " + this.relativeTo;
	}

	RelAllocation allocation() {
		return getPointer().getAllocation().relativeTo(
				getRelativeTo().getAllocation());
	}

}

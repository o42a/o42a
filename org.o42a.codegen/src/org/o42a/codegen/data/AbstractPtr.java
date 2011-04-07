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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class AbstractPtr {

	private final CodeId id;

	public AbstractPtr(CodeId id) {
		this.id = id;
	}

	public final CodeId getId() {
		return this.id;
	}

	public final Ptr<AnyOp> toAny() {
		return new Ptr<AnyOp>(getId().detail("any"), allocationToAny());
	}

	@Override
	public String toString() {
		return getId().toString();
	}

	protected abstract DataAllocation<AnyOp> allocationToAny();

}

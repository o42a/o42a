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

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.util.string.ID;


public abstract class AbstractPtr {

	private final ID id;
	private final boolean ptrToConstant;
	private final boolean isNull;

	public AbstractPtr(ID id, boolean ptrToConstant, boolean isNull) {
		this.id = id;
		this.ptrToConstant = ptrToConstant;
		this.isNull = isNull;
	}

	public final ID getId() {
		return this.id;
	}

	public final boolean isPtrToConstant() {
		return this.ptrToConstant;
	}

	public final boolean isNull() {
		return this.isNull;
	}

	public Ptr<AnyOp> toAny() {

		final ID id = getId().type("any");

		return new Ptr<AnyOp>(id, isPtrToConstant(), isNull()) {
			@Override
			public Ptr<AnyOp> toAny() {
				return this;
			}
			@Override
			protected DataAllocation<AnyOp> createAllocation() {
				return AbstractPtr.this.allocationToAny();
			}
		};
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	protected abstract DataAllocation<AnyOp> allocationToAny();

}

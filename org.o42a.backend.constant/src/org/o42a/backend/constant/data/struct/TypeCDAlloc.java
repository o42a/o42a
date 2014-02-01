/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.data.struct;

import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.TopLevelCDAlloc;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Allocated;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.data.Type;


public class TypeCDAlloc<S extends StructOp<S>> extends TopLevelCDAlloc<S> {

	private final CType<S> underlyingType;

	public TypeCDAlloc(ConstBackend backend, SubData<S> data, Type<S> type) {
		super(backend, data, null, null);
		this.underlyingType = new CType<>(backend, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CType<S> getUnderlyingInstance() {
		return (CType<S>) getUnderlyingAllocated().getContainer();
	}

	@Override
	public String toString() {
		if (this.underlyingType == null) {
			return super.toString();
		}
		return this.underlyingType.getId().toString();
	}

	@Override
	protected Allocated<S, ?> startUnderlyingAllocation(SubData<?> container) {
		return getBackend().getUnderlyingGenerator().getGlobals().allocateType(
				this.underlyingType);
	}

}

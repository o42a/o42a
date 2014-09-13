/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.data;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.Ptr;


final class ToAnyUnderAlloc implements UnderAlloc<AnyOp> {

	private final CDAlloc<?> source;

	ToAnyUnderAlloc(CDAlloc<?> source) {
		this.source = source;
	}

	@Override
	public Ptr<AnyOp> allocateUnderlying(CDAlloc<AnyOp> alloc) {
		return this.source.getUnderlyingPtr().toAny();
	}

	@Override
	public String toString() {
		return "(any*) " + this.source;
	}

}

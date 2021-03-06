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

import static org.o42a.backend.constant.data.DefaultUnderAlloc.DEFAULT_UNDER_ALLOC;

import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;


public interface UnderAlloc<P extends DataPtrOp<P>> {

	@SuppressWarnings("unchecked")
	static <P extends AllocPtrOp<P>> UnderAlloc<P> defaultUnderAlloc() {
		return DEFAULT_UNDER_ALLOC;
	}

	static UnderAlloc<AnyOp> anyUnderAlloc(CDAlloc<?> source) {
		return new ToAnyUnderAlloc(source);
	}

	static UnderAlloc<DataOp> dataUnderAlloc(CDAlloc<?> source) {
		return new ToDataUnderAlloc(source);
	}

	default boolean isDefault() {
		return this == DEFAULT_UNDER_ALLOC;
	}

	Ptr<P> allocateUnderlying(CDAlloc<P> alloc);

}

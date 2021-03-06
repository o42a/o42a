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
package org.o42a.codegen.code.op;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.util.string.ID;


public interface PtrOp<P extends PtrOp<P>> extends Op, OpMeans<P> {

	ID IS_NULL_ID = ID.rawId("is_null");

	BoolOp isNull(ID id, Code code);

	BoolOp eq(ID id, Code code, P other);

	BoolOp ne(ID id, Code code, P other);

	AnyOp toAny(ID id, Code code);

	@Override
	@SuppressWarnings("unchecked")
	default P op() {
		return (P) this;
	}

	default void returnValue(Block code) {
		returnValue(code, true);
	}

	void returnValue(Block code, boolean dispose);

}

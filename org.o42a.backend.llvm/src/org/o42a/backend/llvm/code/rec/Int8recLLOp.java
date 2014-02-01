/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code.rec;

import org.o42a.backend.llvm.code.op.Int8llOp;
import org.o42a.codegen.code.op.Int8op;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.codegen.data.AllocPlace;
import org.o42a.util.string.ID;


public final class Int8recLLOp
		extends IntRecLLOp<Int8recOp, Int8op>
		implements Int8recOp {

	public Int8recLLOp(
			ID id,
			AllocPlace allocPlace,
			long blockPtr,
			long nativePtr) {
		super(id, allocPlace, blockPtr, nativePtr);
	}

	@Override
	public Int8recLLOp create(ID id, long blockPtr, long nativePtr) {
		return new Int8recLLOp(id, null, blockPtr, nativePtr);
	}

	@Override
	protected Int8op createLoaded(ID id, long blockPtr, long nativePtr) {
		return new Int8llOp(id, blockPtr, nativePtr);
	}

}

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
package org.o42a.backend.llvm.code.op;

import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public abstract class LLOp<O extends Op> implements Op {

	private final ID id;
	private final long blockPtr;
	private final long nativePtr;

	public LLOp(ID id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	public final long getBlockPtr() {
		return this.blockPtr;
	}

	public final long getNativePtr() {
		return this.nativePtr;
	}

	public abstract O create(ID id, long blockPtr, long nativePtr);

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

}

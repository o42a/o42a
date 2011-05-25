/*
    Compiler LLVM Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.backend.llvm.code;

import org.o42a.codegen.code.backend.MultiCodePos;


final class LLVMMultiCodePos implements MultiCodePos {

	private final long targetPtr;
	private final long[] targetBlockPtrs;

	LLVMMultiCodePos(long targetPtr, long[] targetBlockPtrs) {
		this.targetPtr = targetPtr;
		this.targetBlockPtrs = targetBlockPtrs;
	}

	public final long getTargetPtr() {
		return this.targetPtr;
	}

	public final long[] getTargetBlockPtrs() {
		return this.targetBlockPtrs;
	}

}

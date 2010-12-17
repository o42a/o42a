/*
    Compiler LLVM Back-end
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.CodePos;


public abstract class LLVMCodePos implements CodePos {

	private final long blockPtr;

	LLVMCodePos(long blockPtr) {
		this.blockPtr = blockPtr;
	}

	public final long getBlockPtr() {
		return this.blockPtr;
	}

	public abstract boolean tailOf(LLVMCode code);

	public static class Head extends LLVMCodePos {

		Head(long blockPtr) {
			super(blockPtr);
		}

		@Override
		public boolean tailOf(LLVMCode code) {
			return false;
		}

	}

	public static class Tail extends LLVMCodePos {

		private final LLVMCode code;

		Tail(LLVMCode code, long blockPtr) {
			super(blockPtr);
			this.code = code;
		}

		@Override
		public boolean tailOf(LLVMCode code) {
			return code == this.code;
		}

	}

}

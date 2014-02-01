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
package org.o42a.backend.llvm.code;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;


public abstract class LLCodePos implements CodePos {

	public abstract long getBlockPtr();

	public abstract boolean tailOf(LLBlock block);

	public static class Head extends LLCodePos {

		private final LLBlock block;

		Head(LLBlock block) {
			this.block = block;
		}

		@Override
		public Block code() {
			return this.block.block();
		}

		@Override
		public long getBlockPtr() {
			return this.block.getFirstBlockPtr();
		}

		@Override
		public boolean tailOf(LLBlock code) {
			return false;
		}

		@Override
		public String toString() {
			return this.block.toString();
		}

	}

	public static class Tail extends LLCodePos {

		private final LLBlock block;
		private long blockPtr;

		Tail(LLBlock block) {
			this.block = block;
		}

		Tail(LLBlock block, long blockPtr) {
			this.block = block;
			this.blockPtr = blockPtr;
		}

		@Override
		public Block code() {
			return this.block.block();
		}

		@Override
		public long getBlockPtr() {
			if (this.blockPtr != 0L) {
				return this.blockPtr;
			}
			return this.blockPtr = this.block.getBlockPtr();
		}

		@Override
		public boolean tailOf(LLBlock code) {
			return code == this.block;
		}

		@Override
		public String toString() {
			return this.block.toString() + "...";
		}

	}

}

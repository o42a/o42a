/*
    Compiler Code Generator
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;


public abstract class OpBlockBase extends Code {

	protected static CodePos unwrapPos(CodePos codePos) {
		if (codePos == null || codePos.getClass() != Head.class) {
			return codePos;
		}
		return ((Head) codePos).unwrap();
	}

	public OpBlockBase(Code enclosing, CodeId name) {
		super(enclosing, name);
	}

	public OpBlockBase(Generator generator, CodeId id) {
		super(generator, id);
	}

	protected abstract CondBlock choose(
			BoolOp condition,
			CodeId trueName,
			CodeId falseName);

	protected void disposeUpTo(Allocator toAllocator) {
		disposeFromTo(getAllocator(), toAllocator);
	}

	protected void disposeFromTo(
			Allocator fromAllocator,
			Allocator toAllocator) {

		Allocator allocator = fromAllocator;

		while (allocator != toAllocator) {
			allocator.allocation().writer().dispose(writer());
			allocator = allocator.getEnclosingAllocator();
			assert allocator != null :
				fromAllocator + " is not inside " + toAllocator;
		}
	}

	protected static final class Head implements CodePos {

		private final Block code;

		public Head(Block code) {
			this.code = code;
		}

		@Override
		public Block code() {
			return this.code;
		}

		@Override
		public String toString() {
			return this.code.toString();
		}

		CodePos unwrap() {
			return this.code.writer().head();
		}

	}

}

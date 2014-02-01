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
package org.o42a.backend.llvm.id;

import org.o42a.backend.llvm.data.LLVMModule;


final class AnyId extends LLVMId {

	private final LLVMId prototype;
	private long nativePtr;
	private long typePtr;

	AnyId(LLVMId prototype) {
		super(prototype.getGlobalId(), prototype.getKind());
		this.prototype = prototype;
	}

	@Override
	public LLVMId getEnclosing() {
		return this.prototype.getEnclosing();
	}

	@Override
	public int getIndex() {
		return this.prototype.getIndex();
	}

	@Override
	public long expression(LLVMModule module) {
		if (this.nativePtr != 0L) {
			return this.nativePtr;
		}
		return this.nativePtr = toAnyPtr(this.prototype.expression(module));
	}

	@Override
	public long typeExpression(LLVMModule module) {
		if (this.typePtr != 0L) {
			return this.typePtr;
		}
		return this.typePtr = toAnyPtr(this.prototype.typeExpression(module));
	}

	@Override
	public LLVMId toAny() {
		return this;
	}

	@Override
	public String toString() {
		return "ANY " + this.prototype;
	}

	@Override
	int[] buildIndices(int len) {
		throw new UnsupportedOperationException();
	}

}

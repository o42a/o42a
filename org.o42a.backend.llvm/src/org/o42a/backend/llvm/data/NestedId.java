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
package org.o42a.backend.llvm.data;


final class NestedId extends LLVMId {

	private final LLVMId enclosing;
	private final int index;
	private long nativePtr;

	NestedId(LLVMId enclosing, int index) {
		super(enclosing.getGlobalId(), enclosing.kind);
		this.enclosing = enclosing;
		this.index = index;
	}

	@Override
	public LLVMId getEnclosing() {
		return this.enclosing;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public long expression(LLVMModule module) {
		if (this.nativePtr != 0L) {
			return this.nativePtr;
		}

		final int[] indices = buildIndices(0);

		assert indices != null :
			"Top-level native pointer missing: " + this;

		return this.nativePtr = LLVMId.expression(
				module.getNativePtr(),
				topLevel().expression(module),
				indices);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = super.hashCode();

		result = prime * result + this.enclosing.hashCode();
		result = prime * result + this.index;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final NestedId other = (NestedId) obj;

		if (!this.enclosing.equals(other.enclosing)) {
			return false;
		}
		if (this.index != other.index) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("getElementPtr(").append(getGlobalId());
		printIndices(out);
		out.append(')');

		return out.toString();
	}

	@Override
	int[] buildIndices(int len) {

		final int[] indexes = this.enclosing.buildIndices(len + 1);

		indexes[len] = this.index;

		return indexes;
	}

}

/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.id;

import static org.o42a.backend.llvm.data.NameLLVMEncoder.NAME_LLVM_ENCODER;

import org.o42a.backend.llvm.data.LLVMModule;


public final class TempLLVMId extends LLVMId {

	private final LLVMId enclosing;
	private final String displayName;
	private LLVMId real;

	TempLLVMId(LLVMId enclosing, String displayName) {
		super(enclosing.getGlobalId(), enclosing.getKind());
		this.enclosing = enclosing;
		this.displayName = displayName;
	}

	@Override
	public final LLVMId getEnclosing() {
		return this.enclosing;
	}

	@Override
	public final int getIndex() {
		return this.real.getIndex();
	}

	@Override
	public final String getDisplayName() {
		if (this.real == null) {
			return this.displayName;
		}
		return this.real.getDisplayName();
	}

	public final LLVMId refine(LLVMId real) {
		assert this.real == null :
			"Temp identifier already refined: " + this;
		return this.real = real;
	}

	@Override
	public final long expression(LLVMModule module) {
		return this.real.expression(module);
	}

	@Override
	public final long typeExpression(LLVMModule module) {
		return this.real.typeExpression(module);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("getElementPtr(");
		out.append(NAME_LLVM_ENCODER.print(getGlobalId()));
		printIndices(out);
		out.append(')');

		return out.toString();
	}

	@Override
	final int[] buildIndices(int len) {
		return this.real.buildIndices(len);
	}

}

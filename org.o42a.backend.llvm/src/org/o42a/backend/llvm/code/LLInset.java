/*
    Compiler LLVM Back-end
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;


class LLInset extends LLCode {

	private final long blockPtr;
	private LLInset prevInset;
	private long nextInstr;
	private boolean exists;

	LLInset(LLCode enclosing, LLInset prevInset, Code code) {
		super(
				enclosing.getModule(),
				enclosing.getFunction(),
				code);
		this.prevInset = prevInset;
		this.blockPtr = enclosing.nextPtr();
		assert prevInset == null || prevInset.blockPtr == this.blockPtr :
			this + " belongs to another block than " + prevInset;
	}

	@Override
	public boolean created() {
		return exists();
	}

	@Override
	public boolean exists() {
		return this.exists;
	}

	@Override
	public final long nextPtr() {
		return this.blockPtr;
	}

	@Override
	public long nextInstr() {
		return this.nextInstr;
	}

	@Override
	protected void addInstr(long instr) {
		super.addInstr(instr);
		this.exists = true;
		if (this.prevInset != null) {
			this.prevInset.nextInstr(instr);
			this.prevInset = null;
		}
	}

	final void nextInstr(long nextInstr) {
		this.nextInstr = nextInstr;
		if (this.prevInset != null) {
			this.prevInset.nextInstr(nextInstr);
		}
	}

}

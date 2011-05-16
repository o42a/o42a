/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.backend.AllocationWriter;


public final class AllocationCode extends Code {

	private final Code enclosing;
	private Code destruction;
	private AllocationWriter writer;

	AllocationCode(Code enclosing, CodeId name) {
		super(enclosing, name != null ? name : enclosing.id().detail("alloc"));
		this.enclosing = enclosing;
		enclosing.go(head());
	}

	public final Code getEnclosing() {
		return this.enclosing;
	}

	@Override
	public final boolean exists() {
		return true;
	}

	public final Code destruction() {
		assert assertIncomplete();
		if (this.destruction != null) {
			return this.destruction;
		}
		this.destruction =
			getEnclosing().addBlock(id().detail("destruct"));
		writer().dispose(this.destruction.writer());
		return this.destruction;
	}

	@Override
	public void done() {
		if (isComplete()) {
			return;
		}

		if (this.destruction != null) {
			go(this.destruction.head());
			this.destruction.go(getEnclosing().tail());
		} else {
			writer().dispose(writer());
			go(getEnclosing().tail());
		}

		super.done();
	}

	@Override
	public final AllocationWriter writer() {
		if (this.writer != null) {
			return this.writer;
		}
		return this.writer =
			getEnclosing().writer().allocationBlock(this, getId());
	}

}

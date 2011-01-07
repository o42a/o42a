/*
    Compiler Code Generator
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;


public abstract class CodeBase {

	private boolean complete;

	public <O extends PtrOp> O allocate(Type<O> type) {
		assertIncomplete();
		return writer().allocateStruct(type.getPointer().getAllocation());
	}

	public final void done() {
		if (complete()) {
			writer().done();
		}
	}

	public abstract CodeWriter writer();

	protected boolean complete() {
		if (this.complete) {
			return false;
		}
		this.complete = true;
		return true;
	}

	protected void assertIncomplete() {
		assert !this.complete :
			this + " already fulfilled";
	}

}

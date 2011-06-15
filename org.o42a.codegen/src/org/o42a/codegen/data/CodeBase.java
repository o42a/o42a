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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class CodeBase {

	protected static <P extends PtrOp<P>> DataAllocation<P> dataAllocation(
			Data<P> data) {
		return data.getAllocation();
	}

	protected static <S extends StructOp<S>> S allocate(
			Code code,
			CodeId id,
			Type<S> type) {
		assert code.assertIncomplete();

		final S result = code.writer().allocateStruct(
				code.opId(id),
				dataAllocation(type.data(code.getGenerator())));

		result.allocated(code, null);

		return result;
	}

	private boolean complete;

	public abstract boolean exists();

	public final boolean isComplete() {
		return this.complete;
	}

	public void done() {
		if (!complete()) {
			return;
		}
		if (exists()) {
			writer().done();
		}
	}

	public abstract CodeWriter writer();

	public final boolean assertIncomplete() {
		assert !this.complete :
			this + " already fulfilled";
		return true;
	}

	protected boolean complete() {
		if (this.complete) {
			return false;
		}
		this.complete = true;
		return true;
	}

}

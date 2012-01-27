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
package org.o42a.codegen.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class FuncPtr<F extends Func<F>> extends AbstractPtr {

	final Signature<F> signature;
	final FuncAllocation<F> allocation;

	FuncPtr(
			CodeId id,
			Signature<F> signature,
			FuncAllocation<F> allocation,
			boolean isNull) {
		super(id, true, isNull);
		this.signature = signature;
		this.allocation = allocation;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public abstract Function<F> getFunction();

	public final FuncAllocation<F> getAllocation() {
		return this.allocation;
	}

	public F op(CodeId id, Code code) {
		code.assertIncomplete();

		final CodeId resultId;

		if (id != null) {
			resultId = code.opId(id);
		} else {
			resultId = getId();
		}

		return this.signature.op(
				code.writer().caller(resultId, this.allocation));
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return this.allocation.toAny();
	}

}

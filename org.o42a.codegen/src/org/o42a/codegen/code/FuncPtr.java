/*
    Compiler Code Generator
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
package org.o42a.codegen.code;

import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.fn.Getter;
import org.o42a.util.string.ID;


public abstract class FuncPtr<F extends Func<F>>
		extends AbstractPtr
		implements FunctionAttributes, Getter<FuncPtr<F>> {

	private final Signature<F> signature;

	FuncPtr(ID id, Signature<F> signature, boolean isNull) {
		super(id, true, isNull);
		this.signature = signature;
	}

	@Override
	public final FuncPtr<F> get() {
		return this;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public abstract Function<F> getFunction();

	public abstract FuncAllocation<F> getAllocation();

	public final F op(ID id, Code code) {
		code.assertIncomplete();

		final ID resultId;

		if (id != null) {
			resultId = code.opId(id);
		} else {
			resultId = getId();
		}

		return this.signature.op(
				code.writer().caller(resultId, getAllocation()));
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return getAllocation().toAny();
	}

}

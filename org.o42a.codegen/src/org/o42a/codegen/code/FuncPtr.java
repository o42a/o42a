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
package org.o42a.codegen.code;

import org.o42a.codegen.code.backend.FuncAllocation;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class FuncPtr<F extends Func> extends AbstractPtr {

	final Signature<F> signature;
	final FuncAllocation<F> allocation;

	FuncPtr(Signature<F> signature, FuncAllocation<F> allocation) {
		this.signature = signature;
		this.allocation = allocation;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	public abstract Function<F> getFunction();

	public F op(Code code) {
		code.assertIncomplete();
		return this.signature.op(code.writer().caller(this.allocation));
	}

	final FuncAllocation<F> getAllocation() {
		return this.allocation;
	}

	@Override
	protected DataAllocation<AnyOp> allocationToAny() {
		return this.allocation.toAny();
	}

}

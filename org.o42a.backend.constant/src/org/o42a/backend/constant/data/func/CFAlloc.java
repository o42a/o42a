/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.backend.constant.data.func;

import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.AnyCDAlloc;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.rec.FuncRecCDAlloc;
import org.o42a.codegen.code.Func;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class CFAlloc<F extends Func<F>>
		implements FuncAllocation<F> {

	private final CSignature<F> underlyingSignature;
	private FuncPtr<F> underlyingPtr;
	private FuncPtr<F> pointer;

	public CFAlloc(
			FuncPtr<F> underlyingPtr,
			CSignature<F> underlyingSignature) {
		this.underlyingPtr = underlyingPtr;
		this.underlyingSignature = underlyingSignature;
	}

	public final ConstBackend getBackend() {
		return getUnderlyingSignature().getBackend();
	}

	public final FuncPtr<F> getPointer() {
		if (this.pointer != null) {
			return this.pointer;
		}
		return this.pointer = pointer();
	}

	public final FuncPtr<F> getUnderlyingPtr() {
		return this.underlyingPtr;
	}

	public final CSignature<F> getUnderlyingSignature() {
		return this.underlyingSignature;
	}

	@Override
	public Signature<F> getSignature() {
		return this.underlyingSignature.getOriginal();
	}

	@Override
	public void write(
			DataWriter writer,
			DataAllocation<FuncOp<F>> detsination) {

		final FuncRecCDAlloc<F> dest = (FuncRecCDAlloc<F>) detsination;

		dest.setConstant(dest.getData().isConstant());
		dest.setValue(getUnderlyingPtr());
	}

	@Override
	public AnyCDAlloc toAny() {
		return new AnyCDAlloc(getBackend(), getUnderlyingPtr().toAny());
	}

	protected abstract FuncPtr<F> pointer();

}

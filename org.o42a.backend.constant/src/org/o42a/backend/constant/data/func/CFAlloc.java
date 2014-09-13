/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import org.o42a.backend.constant.data.*;
import org.o42a.backend.constant.data.rec.FuncRecCDAlloc;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class CFAlloc<F extends Fn<F>>
		implements FuncAllocation<F> {

	private final FuncPtr<F> pointer;
	private final CSignature<F> underlyingSignature;
	private FuncPtr<F> underlyingPtr;

	public CFAlloc(ConstBackend backend, FuncPtr<F> pointer) {
		this.pointer = pointer;
		this.underlyingSignature = backend.underlying(pointer.getSignature());
	}

	public final ConstBackend getBackend() {
		return getUnderlyingSignature().getBackend();
	}

	public final FuncPtr<F> getPointer() {
		return this.pointer;
	}

	public final FuncPtr<F> getUnderlyingPtr() {
		if (this.underlyingPtr != null) {
			return this.underlyingPtr;
		}
		return this.underlyingPtr = createUnderlyingPtr();
	}

	public final CSignature<F> getUnderlyingSignature() {
		return this.underlyingSignature;
	}

	@Override
	public Signature<F> getSignature() {
		return getUnderlyingSignature().getOriginal();
	}

	@Override
	public void write(
			DataWriter writer,
			DataAllocation<FuncOp<F>> destination) {

		final FuncRecCDAlloc<F> dest = (FuncRecCDAlloc<F>) destination;

		dest.setValue(getPointer());
	}

	@Override
	public AnyCDAlloc toAny() {
		return new AnyCDAlloc(getBackend(), getPointer().toAny(), new ToAny());
	}

	protected abstract FuncPtr<F> createUnderlyingPtr();

	private final class ToAny implements UnderAlloc<AnyOp> {

		@Override
		public Ptr<AnyOp> allocateUnderlying(CDAlloc<AnyOp> alloc) {
			return getUnderlyingPtr().toAny();
		}

		@Override
		public String toString() {
			return "(any*) " + CFAlloc.this;
		}

	}

}

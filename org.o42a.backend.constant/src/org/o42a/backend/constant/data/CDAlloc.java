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
package org.o42a.backend.constant.data;

import static org.o42a.backend.constant.data.UnderAlloc.anyUnderAlloc;
import static org.o42a.backend.constant.data.UnderAlloc.dataUnderAlloc;

import org.o42a.backend.constant.data.rec.PtrRecCDAlloc;
import org.o42a.codegen.code.op.DataPtrOp;
import org.o42a.codegen.code.op.RecOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.PtrRec;
import org.o42a.codegen.data.RelPtr;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;
import org.o42a.util.DataLayout;


public abstract class CDAlloc<P extends DataPtrOp<P>>
		implements DataAllocation<P> {

	private final ConstBackend backend;
	private final Ptr<P> pointer;
	final UnderAlloc<P> underAlloc;
	private Ptr<P> underlyingPtr;

	public CDAlloc(
			ConstBackend backend,
			Ptr<P> pointer,
			UnderAlloc<P> underAlloc) {
		this.backend = backend;
		this.underAlloc = underAlloc;
		this.pointer = pointer;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	public final Ptr<P> getPointer() {
		return this.pointer;
	}

	public final Ptr<P> getUnderlyingPtr() {
		if (this.underlyingPtr != null) {
			return this.underlyingPtr;
		}
		return this.underlyingPtr = this.underAlloc.allocateUnderlying(this);
	}

	@Override
	public DataLayout getLayout() {
		return getUnderlyingPtr().getAllocation().getLayout();
	}

	@Override
	public RelAllocation relativeTo(
			RelPtr pointer,
			DataAllocation<?> allocation) {
		return new RelCDAlloc(pointer, (CDAlloc<?>) allocation, this);
	}

	@Override
	public AnyCDAlloc toAny() {
		return new AnyCDAlloc(
				getBackend(),
				getPointer().toAny(),
				anyUnderAlloc(this));
	}

	@Override
	public DataCDAlloc toData() {
		return new DataCDAlloc(
				getBackend(),
				getPointer().toData(),
				dataUnderAlloc(this));
	}

	@Override
	public <RR extends RecOp<RR, PP>, PP extends P> void write(
			DataWriter writer,
			DataAllocation<RR> destination) {

		@SuppressWarnings("unchecked")
		final PtrRecCDAlloc<PtrRec<RR, Ptr<P>>, RR, Ptr<P>> dest =
				(PtrRecCDAlloc<PtrRec<RR, Ptr<P>>, RR, Ptr<P>>) destination;

		dest.setValue(getPointer());
	}

}

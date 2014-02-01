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
package org.o42a.codegen.data;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.FuncOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.util.string.ID;


public final class FuncRec<F extends Func<F>>
		extends Rec<FuncOp<F>, FuncPtr<F>> {

	private final Signature<F> signature;

	FuncRec(SubData<?> enclosing, ID id, Signature<F> signature) {
		super(enclosing, id);
		this.signature = signature;
	}

	@Override
	public final DataType getDataType() {
		if (this.signature.isDebuggable()) {
			return DataType.FUNC_PTR;
		}
		return DataType.CODE_PTR;
	}

	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public final FuncRec<F> setConstant(boolean constant) {
		super.setConstant(constant);
		return this;
	}

	@Override
	public final FuncRec<F> setLowLevel(boolean lowLevel) {
		super.setLowLevel(lowLevel);
		return this;
	}

	@Override
	public final FuncRec<F> setAttributes(RecAttributes attributes) {
		super.setAttributes(attributes);
		return this;
	}

	public final void setNull() {
		setValue(getGenerator().getFunctions().nullPtr(getSignature()));
	}

	@Override
	public FuncOp<F> fieldOf(ID id, Code code, StructOp<?> struct) {
		return struct.func(id, code, this);
	}

	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.allocateFuncPtr(
				getEnclosing().pointer(getGenerator()).getAllocation(),
				this,
				getPointer().getProtoAllocation(),
				getSignature()));
	}

	@Override
	protected void write(DataWriter writer) {
		getValue().get().getAllocation().write(writer, getAllocation());
	}

}

/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code.op;

import static org.o42a.backend.llvm.code.LLCode.llvm;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public class BoolLLOp extends BoolOp implements LLOp<BoolOp> {

	private final ID id;
	private final long blockPtr;
	private final long nativePtr;

	public BoolLLOp(ID id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	@Override
	public long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public <O extends Op> O select(
			ID id,
			Code code,
			O trueValue,
			O falseValue) {
		return llvm(code).select(id, this, trueValue, falseValue);
	}

	@Override
	public void returnValue(Block code) {
		llvm(code).returnValue(this);
	}

	@Override
	public BoolLLOp create(ID id, long blockPtr, long nativePtr) {
		return new BoolLLOp(id, blockPtr, nativePtr);
	}

}

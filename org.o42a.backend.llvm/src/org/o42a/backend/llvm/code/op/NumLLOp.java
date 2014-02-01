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
import org.o42a.codegen.code.op.NumOp;
import org.o42a.util.string.ID;


public abstract class NumLLOp<O extends NumOp<O>, T extends O>
		implements LLOp<O>, NumOp<O> {

	private final ID id;
	private final long blockPtr;
	private final long nativePtr;

	public NumLLOp(ID id, long blockPtr, long nativePtr) {
		this.id = id;
		this.blockPtr = blockPtr;
		this.nativePtr = nativePtr;
	}

	@Override
	public final ID getId() {
		return this.id;
	}

	@Override
	public final long getBlockPtr() {
		return this.blockPtr;
	}

	@Override
	public final long getNativePtr() {
		return this.nativePtr;
	}

	@Override
	public abstract T neg(ID id, Code code);

	@Override
	public abstract T add(ID id, Code code, O summand);

	@Override
	public abstract T sub(ID id, Code code, O subtrahend);

	@Override
	public abstract T mul(ID id, Code code, O multiplier);

	@Override
	public abstract T div(ID id, Code code, O divisor);

	@Override
	public abstract T rem(ID id, Code code, O divisor);

	@Override
	public abstract Int8llOp toInt8(ID id, Code code);

	@Override
	public abstract Int16llOp toInt16(ID id, Code code);

	@Override
	public abstract Int32llOp toInt32(ID id, Code code);

	@Override
	public abstract Int64llOp toInt64(ID id, Code code);

	@Override
	public abstract Fp32llOp toFp32(ID id, Code code);

	@Override
	public abstract Fp64llOp toFp64(ID id, Code code);

	@Override
	public abstract T create(ID id, long blockPtr, long nativePtr);

	@Override
	public void returnValue(Block code) {
		llvm(code).returnValue(this);
	}

	@Override
	public String toString() {
		return this.id.toString();
	}

}

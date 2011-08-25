/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code.rec;

import static org.o42a.codegen.data.AllocClass.AUTO_ALLOC_CLASS;

import org.o42a.backend.llvm.code.op.Fp32llOp;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.Fp32op;
import org.o42a.codegen.code.op.Fp32recOp;
import org.o42a.codegen.data.AllocClass;


public final class Fp32recLLOp
		extends RecLLOp<Fp32recOp, Fp32op>
		implements Fp32recOp {

	public Fp32recLLOp(
			CodeId id,
			AllocClass allocClass,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
	}

	@Override
	public Fp32recLLOp create(CodeId id, long blockPtr, long nativePtr) {
		return new Fp32recLLOp(id, AUTO_ALLOC_CLASS, blockPtr, nativePtr);
	}

	@Override
	protected Fp32op createLoaded(
			CodeId id,
			long blockPtr,
			long nativePtr) {
		return new Fp32llOp(id, blockPtr, nativePtr);
	}

}
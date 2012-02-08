/*
    Compiler LLVM Back-end
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
package org.o42a.backend.llvm.code.rec;

import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.code.op.StructRecOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Type;


public final class StructRecLLOp<S extends StructOp<S>>
		extends RecLLOp<StructRecOp<S>, S>
		implements StructRecOp<S> {

	private final Type<S> type;

	public StructRecLLOp(
			CodeId id,
			AllocClass allocClass,
			Type<S> type,
			long blockPtr,
			long nativePtr) {
		super(id, allocClass, blockPtr, nativePtr);
		this.type = type;
	}

	@Override
	public StructRecLLOp<S> create(CodeId id, long blockPtr, long nativePtr) {
		return new StructRecLLOp<S>(
				id,
				null,
				this.type,
				blockPtr,
				nativePtr);
	}

	@Override
	protected S createLoaded(CodeId id, long blockPtr, long nativePtr) {
		return this.type.op(new LLStruct<S>(
				id,
				null,
				this.type,
				blockPtr,
				nativePtr));
	}

}

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
package org.o42a.backend.llvm.data;

import org.o42a.backend.llvm.code.LLVMFunction;


abstract class TopLevelId extends LLVMId {

	TopLevelId(String globalId, LLVMIdKind kind) {
		super(globalId, kind);
	}

	@Override
	public LLVMId getEnclosing() {
		return null;
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public String toString() {
		return getGlobalId();
	}

	@Override
	int[] buildIndices(int len) {
		if (len == 0) {
			return null;
		}
		return new int[len];
	}

	static final class TypeId extends TopLevelId {

		private final TypeAllocation<?> typeAllocation;
		private long nativePtr;

		TypeId(TypeAllocation<?> typeAllocation) {
			super(typeAllocation.getType().getId(), LLVMIdKind.TYPE);
			this.typeAllocation = typeAllocation;
		}

		@Override
		public String toString() {
			return getGlobalId();
		}

		@Override
		public long expression(LLVMModule module) {
			if (this.nativePtr != 0L) {
				return this.nativePtr;
			}
			return this.nativePtr = typeExpression(
					module.getNativePtr(),
					this.typeAllocation.getTypePtr());
		}

	}

	static final class GlobalId extends TopLevelId {

		private final ContainerAllocation<?> globalAllocation;

		GlobalId(String globalId, ContainerAllocation<?> globalAllocation) {
			super(globalId, LLVMIdKind.DATA);
			this.globalAllocation = globalAllocation;
		}

		@Override
		public long expression(LLVMModule module) {
			return this.globalAllocation.getNativePtr();
		}

	}

	static final class DataId extends TopLevelId {

		private long nativePtr;

		DataId(String id, long nativePtr) {
			super(id, LLVMIdKind.DATA);
			this.nativePtr = nativePtr;
		}

		@Override
		public long expression(LLVMModule module) {
			return this.nativePtr;
		}

	}

	static final class CodeId extends TopLevelId {

		private final LLVMFunction<?> function;

		CodeId(LLVMFunction<?> function) {
			super(function.getName(), LLVMIdKind.CODE);
			this.function = function;
		}

		@Override
		public long expression(LLVMModule module) {
			return this.function.getFunctionPtr();
		}

	}

	static final class ExternId extends TopLevelId {

		private long nativePtr;

		ExternId(String name, long nativePtr) {
			super(name, LLVMIdKind.CODE);
			this.nativePtr = nativePtr;
		}

		@Override
		public long expression(LLVMModule module) {
			return this.nativePtr;
		}

	}

	static final class NullId extends TopLevelId {

		private long nativePtr;

		NullId(long nativePtr, boolean function) {
			super(null, function ? LLVMIdKind.CODE : LLVMIdKind.DATA);
			this.nativePtr = nativePtr;
		}

		@Override
		public long expression(LLVMModule module) {
			return this.nativePtr;
		}

		@Override
		public String toString() {
			return "NULL";
		}

	}

}

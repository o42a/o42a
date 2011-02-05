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
import org.o42a.codegen.CodeId;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;


public abstract class LLVMId {

	public static LLVMId nullId(long nativePtr, boolean function) {
		return new TopLevelId.NullId(nativePtr, function);
	}

	public static LLVMId typeId(TypeAllocation<?> typeAllocation) {
		return new TopLevelId.TypeId(typeAllocation);
	}

	public static LLVMId dataId(
			CodeId globalId,
			ContainerAllocation<?> globalContainer) {
		return new TopLevelId.GlobalId(globalId, globalContainer);
	}

	public static LLVMId dataId(CodeId id, long nativePtr) {
		return new TopLevelId.DataId(id, nativePtr);
	}

	public static LLVMId codeId(LLVMFunction<?> function) {
		return new TopLevelId.CodeId(function);
	}

	public static LLVMId codeId(CodeId id, long nativePtr) {
		return new TopLevelId.ExternId(id, nativePtr);
	}

	private final CodeId globalId;
	final LLVMIdKind kind;

	LLVMId(CodeId globalId, LLVMIdKind kind) {
		this.globalId = globalId;
		this.kind = kind;
	}

	public CodeId getGlobalId() {
		return this.globalId;
	}

	public abstract LLVMId getEnclosing();

	public abstract int getIndex();

	public LLVMId addIndex(int index) {
		return new NestedId(this, index);
	}

	public abstract long expression(LLVMModule module);

	public LLVMId toAny() {
		return new AnyId(this);
	}

	public void write(DataWriter writer) {

		final LLVMDataWriter llvmWriter = (LLVMDataWriter) writer;

		switch (this.kind) {
		case DATA:
			llvmWriter.writeDataId(this);
			return;
		case CODE:
			llvmWriter.writeCodeId(this);
			return;
		case TYPE:
			throw new IllegalStateException("Type pointer not be written");
		}
		throw new IllegalStateException();
	}

	public RelAllocation relativeTo(DataAllocation<?> other) {

		final LLVMAllocation llvmAllocation = (LLVMAllocation) other;

		return new LLVMRelAllocation(this, llvmAllocation.llvmId());
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result =
			prime * result
			+ ((this.globalId == null) ? 0 : this.globalId.hashCode());
		result = prime * result + this.kind.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final LLVMId other = (LLVMId) obj;

		if (this.globalId == null) {
			if (other.globalId != null) {
				return false;
			}
		} else if (!this.globalId.equals(other.globalId)) {
			return false;
		}
		if (this.kind != other.kind) {
			return false;
		}
		return true;
	}

	final long relativeExpression(LLVMModule module, LLVMId relativeTo) {
		return relativeExpression(
				expression(module),
				relativeTo.expression(module));
	}

	final void printIndices(StringBuilder out) {

		final LLVMId enclosing = getEnclosing();

		if (enclosing != null) {
			enclosing.printIndices(out);
			out.append(", ").append(getIndex());
		}
	}

	final LLVMId topLevel() {

		final LLVMId enclosing = getEnclosing();

		return enclosing == null ? this : enclosing.topLevel();
	}

	abstract int[] buildIndices(int len);

	static native long typeExpression(long modulePtr, long typeId);

	static native long expression(
			long modulePtr,
			long globalId,
			int[] indexes);

	static native long relativeExpression(long idPtr, long toPtr);

	static native long toAnyPtr(long pointerPtr);

}

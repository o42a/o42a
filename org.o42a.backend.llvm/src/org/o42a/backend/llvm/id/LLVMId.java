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
package org.o42a.backend.llvm.id;

import org.o42a.backend.llvm.code.LLFunction;
import org.o42a.backend.llvm.data.LLVMDataWriter;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.data.alloc.*;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataWriter;
import org.o42a.codegen.data.backend.RelAllocation;
import org.o42a.util.string.ID;


public abstract class LLVMId {

	public static LLVMId nullId(long nativePtr, boolean function) {
		return new NullLLVMId(nativePtr, function);
	}

	public static LLVMId typeId(TypeLLAlloc<?> typeAllocation) {
		return new TypeLLVMId(typeAllocation);
	}

	public static LLVMId systemTypeId(SystemTypeLLAlloc typeAllocation) {
		return new SystemTypeLLVMId(typeAllocation);
	}

	public static LLVMId dataId(
			ID globalId,
			ContainerLLDAlloc<?> globalContainer) {
		return new GlobalLLVMId(globalId, globalContainer);
	}

	public static LLVMId dataId(ID id, long nativePtr) {
		return new DataLLVMId(id, nativePtr);
	}

	public static LLVMId functionId(LLFunction<?> function) {
		return new FunctionLLVMId(function);
	}

	public static LLVMId extenFuncId(ID id, long nativePtr) {
		return new ExternFuncLLVMId(id, nativePtr);
	}

	private final ID globalId;
	private final LLVMIdKind kind;

	LLVMId(ID globalId, LLVMIdKind kind) {
		this.globalId = globalId;
		this.kind = kind;
	}

	public ID getGlobalId() {
		return this.globalId;
	}

	public abstract LLVMId getEnclosing();

	public abstract int getIndex();

	public String getDisplayName() {
		return Integer.toString(getIndex());
	}

	public LLVMId addIndex(int index) {
		return new NestedId(this, index);
	}

	public TempLLVMId addTemp(String displayName) {
		return new TempLLVMId(this, displayName);
	}

	public abstract long expression(LLVMModule module);

	public abstract long typeExpression(LLVMModule module);

	public LLVMId toAny() {
		return new AnyId(this);
	}

	public void write(DataWriter writer, DataAllocation<?> destination) {

		final LLVMDataWriter llvmWriter = (LLVMDataWriter) writer;

		switch (this.kind) {
		case DATA:
			llvmWriter.writeDataId(destination, this);
			return;
		case CODE:
			llvmWriter.writeCodeId(destination, this);
			return;
		case TYPE:
			throw new IllegalStateException("Type pointer can not be written");
		}
		throw new IllegalStateException();
	}

	public RelAllocation relativeTo(DataAllocation<?> other) {

		final LLAlloc llvmAllocation = (LLAlloc) other;

		return new RelLLDAlloc(this, llvmAllocation.llvmId());
	}

	public final long relativeExpression(LLVMModule module, LLVMId relativeTo) {
		return relativeExpression(
				typeExpression(module),
				relativeTo.typeExpression(module));
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

	final LLVMIdKind getKind() {
		return this.kind;
	}

	final void printIndices(StringBuilder out) {

		final LLVMId enclosing = getEnclosing();

		if (enclosing != null) {
			enclosing.printIndices(out);
			out.append(", ").append(getDisplayName());
		}
	}

	final LLVMId topLevel() {

		final LLVMId enclosing = getEnclosing();

		return enclosing == null ? this : enclosing.topLevel();
	}

	abstract int[] buildIndices(int len);

	static native long typeExpression(long typePtr);

	static native long expression(
			long modulePtr,
			long globalId,
			int[] indexes);

	static native long relativeExpression(long idPtr, long toPtr);

	static native long toAnyPtr(long pointerPtr);

}

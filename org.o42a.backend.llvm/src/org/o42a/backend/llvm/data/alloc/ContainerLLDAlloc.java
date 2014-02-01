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
package org.o42a.backend.llvm.data.alloc;

import static org.o42a.util.DataAlignment.ALIGN_1;
import static org.o42a.util.DataAlignment.maxAlignmentBelowSize;

import org.o42a.backend.llvm.code.LLCode;
import org.o42a.backend.llvm.code.LLStruct;
import org.o42a.backend.llvm.data.LLVMDataAllocator;
import org.o42a.backend.llvm.data.LLVMModule;
import org.o42a.backend.llvm.id.LLVMId;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AllocClass;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.Type;
import org.o42a.util.DataAlignment;
import org.o42a.util.DataLayout;
import org.o42a.util.string.ID;


public abstract class ContainerLLDAlloc<S extends StructOp<S>>
		extends LLDAlloc<S> {

	private final Type<S> type;
	private final long typePtr;
	private DataLayout layout;
	private DataLayout realLayout;
	private long nativePtr;
	private int nextIndex;
	private boolean typeAllocated;

	public ContainerLLDAlloc(
			LLVMModule module,
			long typePtr,
			long typeDataPtr,
			ContainerLLDAlloc<?> enclosing,
			Type<S> type) {
		super(module, enclosing);
		assert typePtr != 0L :
			"Type not created";

		this.typePtr = typePtr;
		this.nativePtr = typeDataPtr;
		this.type = type;
		this.layout =
				new DataLayout(0, type.requiredAlignment().getAlignment());

		if (typeDataPtr != 0L) {
			// New structure construction.
			this.typeAllocated = false;
		} else {
			// Construct an instance of the given type.
			final Ptr<S> typePointer = type.pointer(module.getGenerator());
			final ContainerLLDAlloc<?> typeAlloc;
			final ContainerLLDAlloc<?> protoAlloc =
					(ContainerLLDAlloc<?>) typePointer.getProtoAllocation();

			if (protoAlloc != null) {
				typeAlloc = protoAlloc;
			} else {
				typeAlloc = (ContainerLLDAlloc<?>) typePointer.getAllocation();
			}

			assert typeAlloc.isTypeAllocated() :
				"Can not instantiate the not allocated type "
				+ typeAlloc.getType();

			this.typeAllocated = true;
			this.layout = typeAlloc.getLayout();
			this.realLayout = typeAlloc.getRealLayout();
		}
	}

	public final long getTypePtr() {
		return this.typePtr;
	}

	public final boolean isTypeAllocated() {
		return this.typeAllocated;
	}

	public final void typeAllocated() {
		assert !isTypeAllocated() :
			"Type already allocated";
		this.typeAllocated = true;
	}

	public final long getNativePtr() {
		assert isTypeAllocated() :
			"Type not allocated yet";
		return this.nativePtr;
	}

	public void setNativePtr(long nativePtr) {
		assert isTypeAllocated() :
			"Type not allocated yet";
		this.nativePtr = nativePtr;

		if (this.realLayout != null) {
			return;
		}

		final DataLayout oldLayout = this.layout;

		this.layout = this.layout.roundToAlignment();
		this.realLayout = getModule().dataAllocator().structLayout(this);

		assert getLayout().size() == getRealLayout().size()
				&& getRealLayout().alignment().getBytes()
				<= getLayout().alignment().getBytes():
			"The real and calculated data layouts of type "
			+ getType() + " are not compatible. Real: " + getRealLayout()
			+ ", calculated: " + getLayout() + " (" + oldLayout + ')';

	}

	public final long getTypeDataPtr() {
		assert !isTypeAllocated() :
			"Type already allocated";
		return this.nativePtr;
	}

	public final Type<S> getType() {
		return this.type;
	}

	@Override
	public final DataLayout getLayout() {
		assert isTypeAllocated() :
			getType() + " is not allocated yet";
		return this.layout;
	}

	public final DataLayout getRealLayout() {
		assert isTypeAllocated() :
			getType() + " is not allocated yet";
		return this.realLayout;
	}

	@Override
	public S op(ID id, AllocClass allocClass, CodeWriter writer) {

		final LLCode code = (LLCode) writer;

		return getType().op(new LLStruct<>(
				id,
				allocClass.allocPlace(code.code()),
				getType(),
				code.nextPtr(),
				llvmId().expression(code.getModule())));
	}

	public void layout(ContainerLLDAlloc<?> type) {
		assert !isTypeAllocated() :
			getType() + " is already allocated";
		alignStructField(type);
		unionLayout(type.getLayout());
	}

	public void layout(DataLayout layout) {
		assert !isTypeAllocated() :
			getType() + " already allocated";
		unionLayout(layout);
	}

	final LLVMId nextId() {
		return llvmId().addIndex(this.nextIndex++);
	}

	private void alignStructField(ContainerLLDAlloc<?> type) {

		final DataAlignment typeAlignment = type.getLayout().alignment();

		if (typeAlignment.getBytes() != 1) {
			// One-byte aligned data does not require alignment.
			alignNotPackedStructField(type, typeAlignment);
		} else if (getType().isPacked() && !type.getType().isPacked()) {
			throw new IllegalArgumentException(
					"Packed type " + getType()
					+ " can not contain a field of non-packed type "
					+ type);
		}
	}

	private void alignNotPackedStructField(
			ContainerLLDAlloc<?> type,
			DataAlignment typeAlignment) {

		final LLVMDataAllocator allocator = getModule().dataAllocator();
		final int realAlignment =
				type.getRealLayout().alignment().getBytes();
		final int alignedOffset = this.layout.alignedOffset(typeAlignment);
		int offset = alignedOffset;

		while (offset >= realAlignment) {

			final int allocate =
					maxAlignmentBelowSize(offset).getBytes();

			if (!allocator.allocateField(getTypeDataPtr(), allocate)) {
				throw new IllegalArgumentException(
						"Can not allocate the type " + getType()
						+ ": can not allocate a field"
						+ " with size and alignment " + allocate);
			}
			offset -= allocate;
			this.layout = new DataLayout(
					this.layout.size() + allocate,
					this.layout.alignment());
			nextId();// Identifier is not needed, but should be allocated.
		}
	}

	private void unionLayout(DataLayout layout) {
		if (getType().isPacked()) {
			this.layout = new DataLayout(
					this.layout.size() + layout.size(),
					ALIGN_1);
		} else {
			this.layout = this.layout.union(layout);
		}
	}

}

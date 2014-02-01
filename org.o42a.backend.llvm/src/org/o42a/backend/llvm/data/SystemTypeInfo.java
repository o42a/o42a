/*
    Compiler LLVM Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.backend.llvm.data.LLVMDataAllocator.*;
import static org.o42a.util.DataAlignment.maxAlignmentBelowSize;

import java.util.HashMap;

import org.o42a.backend.llvm.data.alloc.SystemTypeLLAlloc;
import org.o42a.codegen.data.SystemType;
import org.o42a.util.DataAlignment;
import org.o42a.util.DataLayout;


enum SystemTypeInfo {

	PTHREAD("pthread_t") {

		@Override
		protected DataLayout layout() {
			return new DataLayout(pthreadLayout());
		}

	},

	PTHREAD_MUTEX("pthread_mutex_t") {

		@Override
		protected DataLayout layout() {
			return new DataLayout(pthreadMutexLayout());
		}

	},

	PTHREAD_COND("pthread_cond_t") {

		@Override
		protected DataLayout layout() {
			return new DataLayout(pthreadCondLayout());
		}

	},

	GC_BLOCK_PADDING("o42a_gc_block_padding_t") {

		@Override
		protected DataLayout layout() {

			final int padding = gcBlockPadding();

			return new DataLayout(padding, maxAlignmentBelowSize(padding));
		}

	};

	public static SystemTypeLLAlloc allocateSystemType(
			LLVMDataAllocator allocator,
			SystemType systemType) {

		final String typeId = systemType.getId().toString();
		final SystemTypeInfo systemTypeInfo = Registry.types.get(typeId);

		if (systemTypeInfo == null) {
			throw new IllegalArgumentException(
					"Unsupported system type: " + systemType);
		}

		return systemTypeInfo.allocate(allocator, systemType);
	}

	private final String id;

	SystemTypeInfo(String id) {
		this.id = id;
	}

	public final String getId() {
		return this.id;
	}

	protected abstract DataLayout layout();

	private final SystemTypeLLAlloc allocate(
			final LLVMDataAllocator allocator,
			final SystemType systemType) {

		final LLVMModule module = allocator.getModule();
		final long modulePtr = module.getNativePtr();
		final DataLayout layout = layout();

		if (layout.size() == 0) {
			return new SystemTypeLLAlloc(module, systemType, layout, 0L);
		}

		final NativeBuffer ids = module.ids();
		final long typePtr = createType(
				modulePtr,
				ids.write(systemType.getId()),
				ids.length());
		final long dataPtr = createTypeData(modulePtr);

		fillTypeData(allocator, systemType, dataPtr, layout);
		refineType(
				typePtr,
				dataPtr,
				layout.alignment() == DataAlignment.ALIGN_1);

		assert assertLayoutCorrect(systemType, modulePtr, layout, typePtr);

		return new SystemTypeLLAlloc(module, systemType, layout, typePtr);
	}

	private static boolean assertLayoutCorrect(
			final SystemType systemType,
			final long modulePtr,
			final DataLayout layout,
			final long typePtr) {

		final DataLayout actualLayout =
				new DataLayout(structLayout(modulePtr, typePtr));

		assert layout.equals(actualLayout) :
			"Actual data layout (" + actualLayout + ") of " + systemType
			+ " differs from the requested one (" + layout + ")";

		return true;
	}

	private static void fillTypeData(
			final LLVMDataAllocator allocator,
			final SystemType systemType,
			final long dataPtr,
			final DataLayout layout) {

		final short alignment = layout.alignment().getBytes();
		int size = layout.size();

		assert size >= alignment :
			"Illegal data layout: " + layout;

		while (size > 0) {

			final int allocate = Math.min(size, alignment);

			size -= alignment;
			if (!allocator.allocateField(dataPtr, allocate)) {
				throw new IllegalArgumentException(
						"Can not allocate the system type " + systemType
						+ ": can not allocate a field"
						+ " with size and alignment " + allocate);
			}
		}
	}

	private static final class Registry {

		private static final HashMap<String, SystemTypeInfo> types;

		static {

			final SystemTypeInfo[] all = values();

			types = new HashMap<>(all.length);
			for (SystemTypeInfo info : all) {
				types.put(info.getId(), info);
			}
		}

	}

	private static native int pthreadLayout();

	private static native int pthreadMutexLayout();

	private static native int pthreadCondLayout();

	private static native int gcBlockPadding();

}

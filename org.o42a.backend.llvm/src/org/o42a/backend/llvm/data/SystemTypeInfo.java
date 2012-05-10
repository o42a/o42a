/*
    Compiler LLVM Back-end
    Copyright (C) 2012 Ruslan Lopatin

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

import java.util.HashMap;

import org.o42a.backend.llvm.data.alloc.SystemTypeLLAlloc;
import org.o42a.codegen.data.DataLayout;
import org.o42a.codegen.data.SystemType;
import org.o42a.util.DataAlignment;


enum SystemTypeInfo {

	PTHREAD_MUTEX("pthread_mutex_t") {

		@Override
		protected int layout(long modulePtr) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

	};

	public static SystemTypeLLAlloc allocateSystemType(
			LLVMDataAllocator allocator,
			SystemType systemType) {

		final String typeId =
				systemType.codeId(allocator.getModule().getGenerator()).getId();
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

	protected abstract int layout(long modulePtr);

	private final SystemTypeLLAlloc allocate(
			final LLVMDataAllocator allocator,
			final SystemType systemType) {

		final LLVMModule module = allocator.getModule();
		final long modulePtr = module.getNativePtr();
		final DataLayout layout = new DataLayout(layout(modulePtr));
		final NativeBuffer ids = module.ids();
		final long typePtr = createType(
				modulePtr,
				ids.writeCodeId(systemType.codeId(module.getGenerator())),
				ids.length());
		final long dataPtr = createTypeData(modulePtr);

		fillTypeData(modulePtr, dataPtr, layout);
		refineType(
				typePtr,
				dataPtr,
				layout.getAlignment() == DataAlignment.ALIGN_1);

		return new SystemTypeLLAlloc(module, systemType, layout, typePtr);
	}

	private static void fillTypeData(
			final long modulePtr,
			final long dataPtr,
			final DataLayout layout) {

		final short alignment = layout.getAlignment().getBytes();
		int size = layout.getSize();

		assert size >= alignment :
			"Illegal data layout: " + layout;

		while (size > 0) {

			final int allocate = Math.min(size, alignment);

			size -= alignment;
			allocateInt(modulePtr, dataPtr, (short) (allocate << 3));
		}
	}

	private static final class Registry {

		private static final HashMap<String, SystemTypeInfo> types;

		static {

			final SystemTypeInfo[] all = values();

			types = new HashMap<String, SystemTypeInfo>(all.length);
			for (SystemTypeInfo info : all) {
				types.put(info.getId(), info);
			}
		}

	}

}

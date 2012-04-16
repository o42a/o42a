/*
    Compiler LLVM Back-end
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import java.nio.ByteBuffer;

import org.o42a.codegen.CodeId;


public final class NativeBuffer {

	private ByteBuffer buffer;
	private long nativePtr;

	public NativeBuffer(int capacity) {
		allocate(capacity);
	}

	public final long nativePtr() {
		return this.nativePtr;
	}

	public final int length() {
		return this.buffer.position();
	}

	public final long writeCodeId(CodeId id) {
		if (id == null) {
			this.buffer.rewind();
			this.buffer.limit(0);
			return 0L;
		}
		id.write(reset(id.length()));
		return this.nativePtr;
	}

	private final ByteBuffer reset(int capacity) {
		if (this.buffer.capacity() < capacity) {
			allocate(((capacity + 255) >> 8) << 8);
			return this.buffer;
		}
		this.buffer.clear();
		return this.buffer;
	}

	private final void allocate(int capacity) {
		this.buffer = ByteBuffer.allocateDirect(capacity);
		this.nativePtr = LLVMModule.bufferPtr(this.buffer);
	}

}

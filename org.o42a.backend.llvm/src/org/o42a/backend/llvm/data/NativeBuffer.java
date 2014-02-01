/*
    Compiler LLVM Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.backend.llvm.data.LLVMModule.bufferPtr;
import static org.o42a.backend.llvm.data.NameLLVMEncoder.NAME_LLVM_ENCODER;

import java.nio.ByteBuffer;

import org.o42a.util.string.CPWriter;
import org.o42a.util.string.ID;


public final class NativeBuffer {

	private final NativeCPWriter writer;

	public NativeBuffer(int capacity) {
		this.writer = new NativeCPWriter(capacity);
	}

	public final int length() {
		return this.writer.buffer.position();
	}

	public final long write(ID id) {
		if (id == null) {
			this.writer.buffer.rewind();
			this.writer.buffer.limit(0);
			return 0L;
		}
		this.writer.buffer.clear();

		NAME_LLVM_ENCODER.write(this.writer, id);

		return this.writer.nativePtr;
	}

	private static final class NativeCPWriter extends CPWriter {

		private ByteBuffer buffer;
		private long nativePtr;

		NativeCPWriter(int capacity) {
			this.buffer = allocate(capacity);
		}

		@Override
		public void expandCapacity(int size) {

			final int position = this.buffer.position();
			final int oldCapacity = this.buffer.capacity();
			final int requiredCapacity = position + size;

			if (requiredCapacity <= oldCapacity) {
				// Current capacity is enough.
				return;
			}

			// Allocate new buffer.
			final int newCapacity = ((requiredCapacity + 255) >> 8) << 8;
			final ByteBuffer newBuffer = allocate(newCapacity);

			if (position != 0) {
				// Copy old buffer contents to the new one.
				this.buffer.flip();
				while (this.buffer.hasRemaining()) {
					newBuffer.put(this.buffer.get());
				}
			}

			this.buffer = newBuffer;
		}

		@Override
		public String toString() {
			if (this.buffer == null) {
				return super.toString();
			}
			return "NativeCPWriter[" + this.buffer + ']';
		}

		@Override
		public void writeCodePoint(int codePoint) {
			assert codePoint <= 127 :
				"Not an ASCII char: '" + ((char) codePoint) + '\'';
			expandCapacity(1);
			this.buffer.put((byte) codePoint);
		}

		private final ByteBuffer allocate(int capacity) {

			final ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

			this.nativePtr = bufferPtr(buffer);

			return buffer;
		}

	}

}

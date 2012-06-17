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

import static org.o42a.backend.llvm.data.LLVMModule.bufferPtr;

import java.nio.ByteBuffer;

import org.o42a.util.string.*;
import org.o42a.util.string.ID.Separator;


public final class NativeBuffer {

	private final static byte[] HEX_DIGITS = {
		'0', '1', '2', '3',
		'4', '5', '6', '7',
		'8', '9', 'A', 'B',
		'C', 'D', 'E', 'F',
	};

	private final NativeNameWriter writer;
	private final NameWriter encoder;

	public NativeBuffer(int capacity) {
		this.writer = new NativeNameWriter(capacity);
		this.encoder = new NameEncoder(this.writer).canonical();
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

		this.encoder.write(id);

		return this.writer.nativePtr;
	}

	private static final class NativeNameWriter extends NameWriter {

		private ByteBuffer buffer;
		private long nativePtr;

		NativeNameWriter(int capacity) {
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
			return "NetiveNameWriter[" + this.buffer + ']';
		}

		@Override
		protected void writeCodePoint(int codePoint) {
			assert codePoint <= 127 :
				"Not an ASCII char: '" + ((char) codePoint) + '\'';
			this.buffer.put((byte) codePoint);
		}

		private final ByteBuffer allocate(int capacity) {

			final ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

			this.nativePtr = bufferPtr(buffer);

			return buffer;
		}

	}

	private static final class NameEncoder extends NameWriterProxy {

		private boolean lastEncoded;

		NameEncoder(NameWriter out) {
			super(out);
		}

		@Override
		public NameWriter write(Name name) {
			if (name.capitalization().isRaw()) {
				out().write(name);
				return this;
			}
			reset();
			return super.write(name);
		}

		@Override
		public NameWriter write(String string) {
			reset();
			return super.write(string);
		}

		@Override
		protected void writeCodePoint(int c) {
			if (isSpecial(c)) {
				appendSpecial(c);
				this.lastEncoded = false;
			} else if (isEncoded(c)) {
				if (this.lastEncoded) {
					closeEncoded();
					this.lastEncoded = false;
				}
				super.writeCodePoint(c);
			} else if (
					(c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z')
					|| c == '_' || c == '-') {
				super.writeCodePoint(c);
				this.lastEncoded = false;
			} else if (c == ' ') {
				super.writeCodePoint('_');
				this.lastEncoded = false;
			} else {
				appendEncoded(c);
				this.lastEncoded = true;
			}
		}

		@Override
		protected void writeSeparator(Separator separator) {
			switch (separator) {
			case NONE:
				return;
			case TOP:
				out().write(".");
				return;
			case SUB:
				out().write(".");
				return;
			case ANONYMOUS:
				out().write(".");
				return;
			case DETAIL:
				out().write("$");
				return;
			case TYPE:
				out().write("$$");
				return;
			case IN:
				out().write(".$");
				return;
			}
			throw new IllegalArgumentException(
					"Unsupported separator: " + separator);
		}

		private void reset() {
			this.lastEncoded = false;
		}

		private boolean isSpecial(int c) {
			if (c == 'X') {
				return true;
			}
			if (c == 'Z' && this.lastEncoded) {
				return true;
			}
			return false;
		}

		private static boolean isEncoded(int c) {
			return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
		}

		private void appendSpecial(int c) {
			expandCapacity(2);
			super.writeCodePoint('X');
			super.writeCodePoint(c);
		}

		protected void appendEncoded(int c) {

			int ch = c;
			byte[] digits = new byte[32];
			int i = 32;

			do {
			    digits[--i] = HEX_DIGITS[ch & 0xF];
			    ch >>>= 4;
			} while (ch != 0);

			expandCapacity(32 + 1 - i);
			super.writeCodePoint('X');
			while (i < 32) {
				super.writeCodePoint(digits[i++]);
			}
		}

		protected void closeEncoded() {
			expandCapacity(1);
			super.writeCodePoint('Z');
		}

	}

}

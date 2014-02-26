/*
    Utilities
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
package org.o42a.util.io;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.toCodePoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;


final class FileSourceReader extends SourceReader {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private final FileInputStream stream;
	private final FileChannel channel;
	private final CharsetDecoder decoder;
	private final ByteBuffer bytes;
	private final CharBuffer chars;
	private long offset;
	private boolean eof;

	FileSourceReader(FileSource source) throws IOException {
		super(source);
		this.stream = new FileInputStream(source.getFile());
		this.channel = this.stream.getChannel();
		this.decoder = source.getCharset().newDecoder();

		final long size = this.stream.getChannel().size();

		if (size > DEFAULT_BUFFER_SIZE) {
			this.bytes = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		} else {
			this.bytes = ByteBuffer.allocate((int) size);
		}
		this.bytes.limit(0);
		this.chars = CharBuffer.allocate(1);
	}

	@Override
	public long offset() {
		return this.offset + this.bytes.position();
	}

	@Override
	public void seek(long offset) throws IOException {
		if (offset < 0 || offset >= this.channel.size()) {
			throw new IllegalArgumentException(
					"Invalid file offset: " + offset);
		}
		this.channel.position(offset);
		this.offset = offset;
		this.bytes.position(0);
		this.bytes.limit(0);
		this.chars.clear();
	}

	@Override
	public int read() throws IOException {

		char highSurrogate = 0;

		for (;;) {
			if (this.bytes.remaining() == 0) {
				// Read more bytes from file.
				this.offset = this.channel.position();
				this.bytes.clear();

				final boolean eof = this.channel.read(this.bytes) < 0;

				this.bytes.flip();
				this.eof = this.channel.position() >= this.channel.size();

				if (eof) {
					return -1;
				}
			}

			// Decode single char.
			this.chars.clear();

			final CoderResult result =
					this.decoder.decode(this.bytes, this.chars, this.eof);

			if (result.isError()) {
				continue;
			}
			if (result.isUnderflow()) {
				if (!this.eof || this.chars.position() == 0) {
					continue;
				}
			}

			final char c = this.chars.get(0);

			if (isHighSurrogate(c)) {
				highSurrogate = c;
				continue;
			}
			if (isLowSurrogate(c)) {
				if (highSurrogate == 0) {
					continue;
				}
				return toCodePoint(highSurrogate, c);
			}

			return c;
		}
	}

	@Override
	public void close() throws IOException {
		this.stream.close();
	}

}

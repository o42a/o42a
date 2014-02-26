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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;


final class URLSourceReader extends SourceReader {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private final InputStream stream;
	private final CharsetDecoder decoder;
	private final ByteBuffer bytes;
	private final CharBuffer chars;
	private long offset;

	URLSourceReader(URLSource source) throws IOException {
		super(source);

		final URLConnection connection = source.getURL().openConnection();

		this.stream = connection.getInputStream();
		this.decoder = Charset.forName("UTF-8").newDecoder();

		final int size = connection.getContentLength();

		if (size > DEFAULT_BUFFER_SIZE) {
			this.bytes = ByteBuffer.wrap(new byte[DEFAULT_BUFFER_SIZE]);
		} else {
			this.bytes = ByteBuffer.wrap(new byte[size]);
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
		if (offset() != 0) {
			throw new IllegalStateException(
					"Source already read. Can't seek");
		}
		this.stream.skip(offset);
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
				this.offset += this.bytes.limit();
				this.bytes.clear();

				final int bytesRead = this.stream.read(this.bytes.array());

				if (bytesRead < 0) {
					return -1;
				}
				this.bytes.position(0);
				this.bytes.limit(bytesRead);
			}

			this.chars.clear();

			// Decode single char.
			final CoderResult result =
					this.decoder.decode(this.bytes, this.chars, false);

			if (result.isError()) {
				continue;
			}
			if (result.isUnderflow() && this.chars.position() == 0) {
				continue;
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

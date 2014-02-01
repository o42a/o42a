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
package org.o42a.util.string;

import static java.lang.Character.charCount;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.o42a.util.DataAlignment;


public class StringCodec {

	private static final char MAX_ASCII_CHAR = 0x7f;

	public static String escapeControlChars(String string) {
		return escapeControlChars(null, string).toString();
	}

	public static CharSequence escapeControlChars(
			StringBuilder builder,
			String string) {

		StringBuilder out = builder;
		final int len = string.length();

		if (out != null) {
			out.ensureCapacity(out.length() + len);
		}
		for (int i = 0; i < len;) {

			final int c = string.codePointAt(i);

			i += Character.charCount(c);

			if (!Character.isISOControl(c)) {
				if (out != null) {
					out.appendCodePoint(c);
				}
				continue;
			}

			switch (c) {
			case '\r':
				if (out == null) {
					out = builder(string, i, len + 2);
				}
				out.append("\\r");
				continue;
			case '\n':
				if (out == null) {
					out = builder(string, i, len + 2);
				}
				out.append("\\n");
				continue;
			case '\t':
				if (out == null) {
					out = builder(string, i, len + 2);
				}
				out.append("\\t");
				continue;
			}

			final String hex = Integer.toHexString(c);

			if (out == null) {
				out = builder(string, i, len + 2 + hex.length());
			}

			out.append(hex);
		}

		if (out != null) {
			return out;
		}

		return string;
	}

	public static DataAlignment bytesPerChar(String string) {

		DataAlignment result = DataAlignment.ALIGN_1;
		final int len = string.length();

		for (int i = 0; i < len; ++i) {

			final int c = string.codePointAt(i);
			final int charCount = Character.charCount(c);

			if (charCount > 1) {
				// More than one character (i.e. one) require 4 bytes..
				return DataAlignment.ALIGN_4;
			}
			if (c > 0xff) {
				// Can not be placed in one byte. Place to two bytes.
				result = DataAlignment.ALIGN_2;
			}
		}

		return result;
	}

	public static int stringToBinary(
			String string,
			byte[] bytes,
			DataAlignment bytesPerChar) {

		final short charSize = bytesPerChar.getBytes();
		final int length = string.length();

		if (length == 0) {
			return 0;
		}

		int b = 0;
		int i = 0;

		do {

			final int c = string.codePointAt(i);
			final int charCount = charCount(c);

			i += charCount;

			bytes[b] = (byte) c;
			if (charSize > 1) {
				bytes[b + 1] = (byte) ((c & 0xFF00) >>> 8);
				if (charCount > 1 && charSize > 2) {
					bytes[b + 2] = (byte) ((c & 0xFF0000) >>> 16);
					bytes[b + 3] = (byte) ((c & 0xFF000000) >>> 24);
				}
			}

			b += charSize;
		} while (i < length);

		return b;
	}

	public static byte[] stringToBinary(String string) {

		final DataAlignment bytesPerChar = bytesPerChar(string);
		final int size = string.length() * bytesPerChar.getBytes();
		final byte[] bytes = new byte[size];
		final int written = stringToBinary(string, bytes, bytesPerChar);

		if (written == size) {
			return bytes;
		}

		final byte[] result = new byte[written];

		System.arraycopy(bytes, 0, result, 0, written);

		return result;
	}

	public static void writeASCII(CharSequence string, ByteBuffer out) {

		final int length = string.length();

		for (int i = 0; i < length; ++i) {

			final char c = string.charAt(i);

			assert c < MAX_ASCII_CHAR :
				"Not an ASCII character: " + c;

			out.put((byte) c);
		}
	}

	public static byte[] nullTermASCIIString(CharSequence string) {

		final int length = string.length();
		final byte[] result = new byte[length + 1];

		for (int i = 0; i < length; ++i) {

			final char c = string.charAt(i);

			assert c < MAX_ASCII_CHAR :
				"Not an ASCII character: " + c;

			result[i] = (byte) c;
		}

		return result;
	}

	public static byte[] nullTermString(Charset charset, String string) {

		final ByteBuffer buffer = charset.encode(string);
		final int len = buffer.limit();
		final byte[] result = new byte[len + 1];// zero-terminated

		buffer.get(result, 0, len);

		return result;
	}

	private static StringBuilder builder(String string, int index, int len) {

		final StringBuilder builder = new StringBuilder(len);

		builder.append(string, 0, index);

		return builder;
	}

	private StringCodec() {
	}

}

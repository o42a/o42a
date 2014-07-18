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

import org.o42a.util.string.*;


public final class NameLLVMEncoder extends NameEncoder {

	public static final NameEncoder NAME_LLVM_ENCODER =
			new NameLLVMEncoder().canonical();

	private final static byte[] HEX_DIGITS = {
		'0', '1', '2', '3',
		'4', '5', '6', '7',
		'8', '9', 'A', 'B',
		'C', 'D', 'E', 'F',
	};

	private NameLLVMEncoder() {
	}

	@Override
	protected void writeName(CPWriter out, Name name) {
		if (name.capitalization().isRaw()) {
			super.writeName(out, name);
			return;
		}
		super.writeName(new ASCIIEncoder(out), name);
	}

	@Override
	protected void writeSeparator(CPWriter out, IDSeparator separator) {
		switch (separator) {
		case NONE:
			return;
		case TOP:
			out.write(".");
			return;
		case SUB:
			out.write(".");
			return;
		case ANONYMOUS:
			out.write(".");
			return;
		case DETAIL:
			out.write("$");
			return;
		case TYPE:
			out.write("$$");
			return;
		case IN:
			out.write(".$");
			return;
		}
		throw new IllegalArgumentException(
				"Unsupported separator: " + separator);
	}

	private static final class ASCIIEncoder extends CPWriterProxy {

		private boolean lastEncoded;

		ASCIIEncoder(CPWriter out) {
			super(out);
		}

		@Override
		public void writeCodePoint(int c) {
			if (isSpecial(c)) {
				appendSpecial(c);
				this.lastEncoded = false;
			} else if (isEncoded(c)) {
				if (this.lastEncoded) {
					closeEncoded();
					this.lastEncoded = false;
				}
				super.writeCodePoint(c);
			} else if (isId(c)) {
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

		private boolean isId(int c) {
			return (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z')
					|| c == '_';
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
			writeASCII('X');
			writeASCII(c);
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
			writeASCII('X');
			while (i < 32) {
				writeASCII(digits[i++]);
			}
		}

		protected void closeEncoded() {
			expandCapacity(1);
			writeASCII('Z');
		}

	}

}

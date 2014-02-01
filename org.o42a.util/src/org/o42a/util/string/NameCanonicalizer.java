/*
    Utilities
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
package org.o42a.util.string;


final class NameCanonicalizer extends NameEncoderProxy {

	NameCanonicalizer(NameEncoder out) {
		super(out);
	}

	@Override
	protected void writeName(CPWriter out, Name name) {

		final Capitalization capitalization = name.capitalization();

		if (capitalization.isCaseSensitive()) {
			super.writeName(out, name);
		} else {
			super.writeName(new CanonicalCPWriter(out, capitalization), name);
		}
	}

	private static final class CanonicalCPWriter extends CPWriterProxy {

		private final Capitalization capitalization;

		CanonicalCPWriter(CPWriter out, Capitalization capitalization) {
			super(out);
			this.capitalization = capitalization;
		}

		@Override
		public void writeCodePoint(int codePoint) {
			super.writeCodePoint(this.capitalization.canonical(codePoint));
		}

	}

}

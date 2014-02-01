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


final class NameDecapitalizer extends NameEncoderProxy {

	NameDecapitalizer(NameEncoder out) {
		super(out);
	}

	@Override
	protected void writeName(CPWriter out, Name name) {

		final Capitalization capitalization = name.capitalization();

		if (capitalization.preservesCapital()) {
			super.writeName(out, name);
			return;
		}

		super.writeName(new DecapitalizerCPWriter(out, capitalization), name);
	}

	private static final class DecapitalizerCPWriter extends CPWriterProxy {

		private Capitalization capitalization;

		DecapitalizerCPWriter(CPWriter out, Capitalization capitalization) {
			super(out);
			this.capitalization = capitalization;
		}

		@Override
		public void writeCodePoint(int codePoint) {
			if (this.capitalization == null) {
				super.writeCodePoint(codePoint);
				return;
			}
			super.writeCodePoint(
					this.capitalization.decapitalizeFirst(codePoint));
			this.capitalization = null;
		}

	}

}

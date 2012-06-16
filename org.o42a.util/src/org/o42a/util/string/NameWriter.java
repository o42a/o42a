/*
    Utilities
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
package org.o42a.util.string;


public abstract class NameWriter {

	/**
	 * Expands a writer capacity.
	 *
	 * @param size an estimated number of characters expected to be written.
	 */
	public void extpandCapacity(int size) {
	}

	public final NameWriter write(ID id) {

		final ID prefix = id.getPrefix();
		final NameWriter writer;
		final NameWriter nextWriter;

		if (prefix != null) {
			nextWriter = write(prefix);
			writer = nextWriter;
		} else {
			nextWriter = decapitalizer();
			writer = this;
		}

		writer.writerSeparator(id.getSeparator());
		writer.write(id.getName());

		return nextWriter;
	}

	public NameWriter write(Name name) {
		return writeString(name.toString());
	}

	public NameWriter write(String string) {
		return writeString(string);
	}

	public final NameWriter canonical() {
		return new CanonicalNameWriter(this);
	}

	public final NameWriter decapitalizer() {
		return new DecapitalizerNameWriter(this);
	}

	public final NameWriter underscored() {
		return new UnderscoredNameWriter(this);
	}

	protected abstract void writeCodePoint(int codePoint);

	protected void writerSeparator(ID.Separator separator) {
		writeString(separator.getDefaultSign());
	}

	private NameWriter writeString(String string) {

		final int len = string.length();

		if (len == 0) {
			return this;
		}

		extpandCapacity(len);

		int i = 0;

		do {

			final int cp = string.codePointAt(i);

			i += Character.charCount(cp);
			writeCodePoint(cp);
		} while (i < len);

		return this;
	}

}

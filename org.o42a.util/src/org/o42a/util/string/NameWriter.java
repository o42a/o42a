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

import org.o42a.util.string.ID.Separator;


public abstract class NameWriter {

	/**
	 * Expands a writer capacity.
	 *
	 * @param size an estimated number of characters to be written.
	 */
	public void expandCapacity(int size) {
	}

	public final NameWriter write(ID id) {

		final Separator[] lastSeparator = new Separator[] {Separator.NONE};
		final NameWriter nextWriter = writeID(lastSeparator, id, true);
		final Separator lastSep = lastSeparator[0];

		if (lastSep == Separator.TOP) {
			nextWriter.writeSeparator(lastSep);
		}

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

	protected void writeSeparator(ID.Separator separator) {
		writeString(separator.getDefaultSign());
	}

	protected void writeRaw(Name name) {
		write(name);
	}

	private NameWriter writeString(String string) {

		final int len = string.length();

		if (len == 0) {
			return this;
		}

		expandCapacity(len);

		int i = 0;

		do {

			final int cp = string.codePointAt(i);

			i += Character.charCount(cp);
			writeCodePoint(cp);
		} while (i < len);

		return this;
	}

	private NameWriter writeID(
			Separator[] lastSeparator,
			ID id,
			boolean decapitalize) {

		final ID prefix = id.getPrefix();
		final NameWriter writer;
		final NameWriter nextWriter;

		if (prefix != null) {
			nextWriter = writeID(lastSeparator, prefix, decapitalize);
			writer = nextWriter;
		} else if (decapitalize) {
			nextWriter = decapitalizer();
			writer = this;
		} else {
			nextWriter = this;
			writer = this;
		}

		final Name name = id.getName();
		final Separator prev = lastSeparator[0];
		final Separator next = id.getSeparator();

		if (name.isEmpty()) {
			if (!prev.discardsNext(next)) {
				if (!next.discardsPrev(prev)) {
					writer.writeSeparator(prev);
				}
				lastSeparator[0] = next;
			}
		} else {
			if (prev.discardsNext(next)) {
				writer.writeSeparator(prev);
			} else if (next.discardsPrev(prev)) {
				writer.writeSeparator(next);
			} else {
				writer.writeSeparator(prev);
				writer.writeSeparator(next);
			}
			lastSeparator[0] = Separator.NONE;
			if (id.isRaw()) {
				writer.writeRaw(name);
			} else {
				writer.write(name);
			}
		}

		final ID suffix = id.getSuffix();

		if (suffix != null) {
			nextWriter.writeID(lastSeparator, suffix, false);
		}

		return nextWriter;
	}

}

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


public class NameEncoder {

	public static final NameEncoder NAME_ENCODER = new NameEncoder();

	protected NameEncoder() {
	}

	public final NameEncoder write(CPWriter out, ID id) {

		final Separator[] lastSeparator = new Separator[] {Separator.NONE};
		final NameEncoder nextWriter = writeID(out, lastSeparator, id, true);
		final Separator lastSep = lastSeparator[0];

		if (lastSep.isTop()) {
			nextWriter.writeSeparator(out, lastSep);
		}

		return nextWriter;
	}

	public final NameEncoder write(CPWriter out, Name name) {
		writeName(out, name);
		return this;
	}

	public final NameEncoder canonical() {
		return new NameCanonicalizer(this);
	}

	public final NameEncoder decapitalized() {
		return new NameDecapitalizer(this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected void writeName(CPWriter out, Name name) {
		out.write(name.toString());
	}

	protected void writeSeparator(CPWriter out, ID.Separator separator) {
		out.write(separator.getDefaultSign());
	}

	private NameEncoder writeID(
			CPWriter out,
			Separator[] lastSeparator,
			ID id,
			boolean decapitalize) {

		final ID prefix = id.getPrefix();
		final NameEncoder writer;
		final NameEncoder nextWriter;
		final Name name = id.getName();
		final boolean decapSuffix;

		if (prefix != null) {
			nextWriter = writeID(out, lastSeparator, prefix, decapitalize);
			writer = nextWriter;
			decapSuffix = decapitalize && this == nextWriter;
		} else if (!decapitalize) {
			nextWriter = this;
			writer = this;
			decapSuffix = false;
		} else if (name.isEmpty()) {
			nextWriter = this;
			writer = this;
			decapSuffix = true;
		} else {
			nextWriter = decapitalized();
			writer = this;
			decapSuffix = false;
		}

		final Separator prev = lastSeparator[0];
		final Separator next = id.getSeparator();

		if (name.isEmpty()) {
			if (!prev.discardsNext(next)) {
				if (!next.discardsPrev(prev)) {
					writer.writeSeparator(out, prev);
				}
				lastSeparator[0] = next;
			}
		} else {
			if (prev.discardsNext(next)) {
				writer.writeSeparator(out, prev);
			} else if (next.discardsPrev(prev)) {
				writer.writeSeparator(out, next);
			} else {
				writer.writeSeparator(out, prev);
				writer.writeSeparator(out, next);
			}
			lastSeparator[0] = Separator.NONE;
			writer.write(out, name);
		}

		final ID suffix = id.getSuffix();

		if (suffix == null) {
			return nextWriter;
		}

		return nextWriter.writeID(out, lastSeparator, suffix, decapSuffix);
	}

}

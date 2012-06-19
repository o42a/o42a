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

import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID.Separator;


public class NameEncoder {

	public static final NameEncoder NAME_ENCODER = new NameEncoder();

	protected NameEncoder() {
	}

	public final NameEncoder write(CPWriter out, ID id) {

		final LastSeparator lastSeparator = new LastSeparator(Separator.NONE);
		final NameEncoder nextEncoder = writeID(out, lastSeparator, id, true);

		if (lastSeparator.getSeparator().isTop()) {
			lastSeparator.write(out, nextEncoder);
		}

		return nextEncoder;
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

	/**
	 * Ends the separator previously written with
	 * {@link #writeSeparator(CPWriter, Separator)}.
	 *
	 * <p>This method is called after the {@link ID#getName() name}
	 * and {@link ID#getSuffix() suffix} written.
	 *
	 * @param out code point writer.
	 * @param separator ID separator.
	 */
	protected void endSeparator(CPWriter out, ID.Separator separator) {
	}

	private NameEncoder writeID(
			CPWriter out,
			LastSeparator lastSeparator,
			ID id,
			boolean decapitalize) {

		final ID prefix = id.getPrefix();
		final NameEncoder encoder;
		final NameEncoder nextEncoder;
		final Name name = id.getName();
		final boolean decapitalizeSuffix;

		if (prefix != null) {
			nextEncoder = writeID(out, lastSeparator, prefix, decapitalize);
			encoder = nextEncoder;
			decapitalizeSuffix = decapitalize && this == nextEncoder;
		} else if (!decapitalize) {
			nextEncoder = this;
			encoder = this;
			decapitalizeSuffix = false;
		} else if (name.isEmpty()) {
			nextEncoder = this;
			encoder = this;
			decapitalizeSuffix = decapitalize;
		} else {
			nextEncoder = decapitalized();
			encoder = this;
			decapitalizeSuffix = false;
		}

		final LastSeparator nextSeparator;
		final Separator prev = lastSeparator.getSeparator();
		final Separator next = id.getSeparator();

		if (name.isEmpty()) {
			if (prev.discardsNext(next)) {
				nextSeparator = lastSeparator;
			} else if (next.discardsPrev(prev)) {
				nextSeparator = new LastSeparator(next);
			} else {
				lastSeparator.write(out, encoder);
				nextSeparator = new LastSeparator(next);
			}
		} else {
			if (prev.discardsNext(next)) {
				nextSeparator = lastSeparator.write(out, encoder);
			} else if (next.discardsPrev(prev)) {
				nextSeparator = new LastSeparator(next).write(out, encoder);
			} else {
				lastSeparator.write(out, encoder);
				nextSeparator = new LastSeparator(next).write(out, encoder);
			}
			encoder.write(out, name);
		}

		final NameEncoder finalEncoder;
		final ID suffix = id.getSuffix();

		if (suffix == null) {
			finalEncoder = nextEncoder;
		} else {
			finalEncoder = nextEncoder.writeID(
					out,
					nextSeparator,
					suffix,
					decapitalizeSuffix);
		}

		if (nextSeparator != lastSeparator) {
			if (!nextSeparator.end(out, encoder)) {
				lastSeparator.replace(nextSeparator.getSeparator());
			}
		}

		return finalEncoder;
	}

	private static final class LastSeparator {

		private Separator separator;
		private Separator[] written = new Separator[0];
		private boolean separatorWritten;

		public LastSeparator(Separator separator) {
			this.separator = separator;
		}

		public final Separator getSeparator() {
			return this.separator;
		}

		public final void replace(Separator separator) {
			this.separator = separator;
			this.separatorWritten = false;
		}

		public final LastSeparator write(CPWriter out, NameEncoder encoder) {
			if (this.separatorWritten) {
				return this;
			}
			this.separatorWritten = true;
			if (!this.separator.isNone()) {
				this.written = ArrayUtil.prepend(this.separator, this.written);
				encoder.writeSeparator(out, this.separator);
				this.separator = Separator.NONE;
			}
			return this;
		}

		public final boolean end(CPWriter out, NameEncoder encoder) {
			for (Separator written : this.written) {
				encoder.endSeparator(out, written);
			}
			return this.separatorWritten;
		}

		@Override
		public String toString() {
			if (this.separator == null) {
				return super.toString();
			}
			return this.separator.toString();
		}

	}

}

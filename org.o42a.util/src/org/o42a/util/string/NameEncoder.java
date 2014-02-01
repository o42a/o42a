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

import org.o42a.util.ArrayUtil;


public class NameEncoder {

	public static final NameEncoder NAME_ENCODER = new NameEncoder();

	protected NameEncoder() {
	}

	public final String print(ID id) {

		final StringCPWriter out = new StringCPWriter();

		write(out, id);

		return out.toString();
	}

	public final String print(Name id) {

		final StringCPWriter out = new StringCPWriter();

		write(out, id);

		return out.toString();
	}

	public final NameEncoder write(CPWriter out, ID id) {

		final LastSeparator lastSeparator = new LastSeparator(IDSeparator.NONE);
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

	protected void writeSeparator(CPWriter out, IDSeparator separator) {
		out.write(separator.getDefaultSign());
	}

	/**
	 * Ends the separator previously written with
	 * {@link #writeSeparator(CPWriter, IDSeparator)}.
	 *
	 * <p>This method is called after the {@link ID#getName() name}
	 * and {@link ID#getSuffix() suffix} written.
	 *
	 * @param out code point writer.
	 * @param separator ID separator.
	 */
	protected void endSeparator(CPWriter out, IDSeparator separator) {
	}

	protected ID expandSubID(SubID subID) {
		return subID.toID();
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
		final IDSeparator prev = lastSeparator.getSeparator();
		final IDSeparator next = id.getSeparator();

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
		final SubID suffix = id.getSuffix();

		if (suffix == null) {
			finalEncoder = nextEncoder;
		} else {
			finalEncoder = nextEncoder.writeID(
					out,
					nextSeparator,
					expandSubID(suffix),
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

		private IDSeparator separator;
		private IDSeparator[] written = new IDSeparator[0];
		private boolean separatorWritten;

		public LastSeparator(IDSeparator separator) {
			this.separator = separator;
		}

		public final IDSeparator getSeparator() {
			return this.separator;
		}

		public final void replace(IDSeparator separator) {
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
				this.separator = IDSeparator.NONE;
			}
			return this;
		}

		public final boolean end(CPWriter out, NameEncoder encoder) {
			for (IDSeparator written : this.written) {
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

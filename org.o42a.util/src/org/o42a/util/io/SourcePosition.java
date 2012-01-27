/*
    Utilities
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import java.util.Formatter;

import org.o42a.util.log.Loggable;


public final class SourcePosition extends Loggable {

	private final Source source;
	private final int line;
	private final int column;
	private final long offset;

	public SourcePosition(Source source) {
		assert source != null :
			"Source not specified";
		this.source = source;
		this.line = 1;
		this.column = 0;
		this.offset = 0;
	}

	public SourcePosition(Source source, int line, int column, long offset) {
		assert source != null :
			"Source not specified";
		this.source = source;
		this.line = line;
		this.column = column;
		this.offset = offset;
	}

	public final Source source() {
		return this.source;
	}

	public final int line() {
		return this.line;
	}

	public final int column() {
		return this.column;
	}

	public final long offset() {
		return this.offset;
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision) {
		formatTo(formatter, true);
	}

	public void formatTo(Formatter formatter, boolean withFile) {
		if (withFile) {
			formatter.format("%s:", source().getName());
		}
		formatter.format("%d,%d(%d)", line(), column(), offset());
	}

	@Override
	public void print(StringBuilder out) {
		print(out, true);
	}

	public void print(StringBuilder out, boolean withFile) {
		if (withFile) {

			final Source source = source();

			if (source != null) {
				out.append(source.getName()).append(':');
			}
		}

		out.append(line()).append(',').append(column());
		out.append('(').append(offset()).append(')');
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + (int) (this.offset ^ (this.offset >>> 32));
		result = prime * result + this.source.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final SourcePosition other = (SourcePosition) obj;

		if (this.offset != other.offset) {
			return false;
		}
		if (!this.source.equals(other.source)) {
			return false;
		}

		return true;
	}

}

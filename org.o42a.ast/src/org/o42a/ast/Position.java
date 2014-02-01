/*
    Abstract Syntax Tree
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.ast;

import org.o42a.util.io.Source;
import org.o42a.util.io.SourcePosition;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public abstract class Position implements LogInfo {

	public abstract Source source();

	public abstract int line();

	public abstract int column();

	public abstract long offset();

	public SourcePosition fix() {
		return new SourcePosition(source(), line(), column(), offset());
	}

	@Override
	public Loggable getLoggable() {
		return fix();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Position)) {
			return false;
		}

		final Position other = (Position) obj;
		final Source source = source();

		if (offset() != other.offset()) {
			return false;
		}
		if (!source.equals(other.source())) {
			return false;
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		final Source source = source();
		final long offset = offset();
		int result = 1;

		result = prime * result + source.hashCode();
		result = prime * result + (int) (offset ^ (offset >>> 32));

		return result;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		print(out, true);

		return out.toString();
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

}

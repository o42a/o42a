/*
    Utilities
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.util.log.LogLocation;


final class LoggableSource extends LogLocation {

	private final Source source;

	LoggableSource(Source source) {
		this.source = source;
	}

	@Override
	public final Source getSource() {
		return this.source;
	}

	@Override
	public final SourcePosition getPosition() {
		return null;
	}

	@Override
	public final SourceRange getRange() {
		return null;
	}

	@Override
	public void formatTo(
			Formatter formatter,
			int flags,
			int width,
			int precision) {
		formatter.format("%s", this.source);
	}

	@Override
	public void print(StringBuilder out) {
		out.append(this.source);
	}

	@Override
	public String toString() {
		if (this.source == null) {
			return super.toString();
		}
		return this.source.toString();
	}

}

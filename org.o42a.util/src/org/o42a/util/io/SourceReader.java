/*
    Utilities
    Copyright (C) 2011 Ruslan Lopatin

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

import java.io.Closeable;
import java.io.IOException;



public abstract class SourceReader implements Closeable {

	private final Source source;

	public SourceReader(Source source) {
		assert source != null :
			"Source not specified";
		this.source = source;
	}

	public final Source source() {
		return this.source;
	}

	public abstract long offset();

	public abstract int read() throws IOException;

	@Override
	public String toString() {
		if (this.source == null) {
			return super.toString();
		}
		return this.source.toString() + '(' + offset() + ')';
	}
}

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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class StringSource extends Source {

	private static final long serialVersionUID = -1835051615367609323L;

	private final String name;
	private final String string;

	public StringSource(String name, String string) {
		this.name = name;
		this.string = string;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Reader open() throws IOException {
		return new StringReader(this.string);
	}

}

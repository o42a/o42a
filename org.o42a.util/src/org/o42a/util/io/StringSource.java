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

import java.io.IOException;


public class StringSource extends Source {

	private final String name;
	private final String string;

	public StringSource(String name, String string) {
		assert name != null :
			"Source name not specified";
		assert string != null :
			"Source text string not specified";
		this.name = name;
		this.string = string;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public boolean isEmpty() {
		return getString().isEmpty();
	}

	public final String getString() {
		return this.string;
	}

	@Override
	public SourceReader open() throws IOException {
		return new StringSourceReader(this);
	}

}

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


final class CanonicalNameWriter extends NameWriterProxy {

	private Capitalization capitalization;

	CanonicalNameWriter(NameWriter out) {
		super(out);
	}

	@Override
	public NameWriter write(Name name) {
		this.capitalization = name.capitalization();
		return super.write(name);
	}

	@Override
	public NameWriter write(String string) {
		out().write(string);
		return this;
	}

	@Override
	protected void writeCodePoint(int codePoint) {
		out().writeCodePoint(this.capitalization.canonical(codePoint));
	}

	@Override
	protected void writerSeparator(Separator separator) {
		out().writerSeparator(separator);
	}

}

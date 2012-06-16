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


public final class ID {

	private final ID prefix;
	private final Separator separator;
	private final Name name;

	private ID(ID prefix, Separator separator, Name name) {
		this.prefix = prefix;
		this.separator = separator;
		this.name = name;
	}

	public final ID getPrefix() {
		return this.prefix;
	}

	public final Separator getSeparator() {
		return this.separator;
	}

	public final Name getName() {
		return this.name;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return super.toString();
		}

		final StringNameWriter out = new StringNameWriter();

		out.write(this);

		return out.toString();
	}

	public enum Separator {

		NONE(""),
		TOP("."),
		SUB(":"),
		ANONYMOUS(":"),
		DETAIL("$"),
		TYPE("$$"),
		IN("::"),
		RAW("");

		private final String defaultSign;

		Separator(String defaultSign) {
			this.defaultSign = defaultSign;
		}

		public String getDefaultSign() {
			return this.defaultSign;
		}

	}

}

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

import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;


final class DescriptionOfID implements SubID {

	private final ID id;
	private final ID description;

	DescriptionOfID(ID id, String text) {
		this.id = id;
		assert text != null :
			"Text to display is not specified";
		this.description = CASE_INSENSITIVE.name(text).toID();
	}

	@Override
	public ID toDisplayID() {
		return this.description;
	}

	@Override
	public ID toID() {
		return this.id;
	}

	@Override
	public String toString() {
		if (this.description == null) {
			return super.toString();
		}
		return this.description.toString();
	}

}

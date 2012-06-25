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

import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;


final class DisplayText implements SubID {

	private final ID text;

	DisplayText(String text) {
		assert text != null :
			"Text to display is not specified";
		this.text = CASE_SENSITIVE.name(text).toID();
	}

	@Override
	public ID toDisplayID() {
		return this.text;
	}

	@Override
	public ID toID() {
		return ID.id();
	}

	@Override
	public String toString() {
		if (this.text == null) {
			return super.toString();
		}
		return this.text.toString();
	}

}

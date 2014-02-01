/*
    Compiler Core
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
package org.o42a.core.object.impl;


public enum ObjectResolution {

	NOT_RESOLVED(0),
	RESOLVING_TYPE(-1),
	TYPE_RESOLVED(1),
	POST_RESOLVED(2),
	RESOLVING_MEMBERS(-3),
	MEMBERS_RESOLVED(3);

	private final int code;

	ObjectResolution(int code) {
		this.code = code;
	}

	public boolean resolved() {
		return this.code >= POST_RESOLVED.code;
	}

	public boolean membersResolved() {
		return (this.code >= MEMBERS_RESOLVED.code
				|| this.code < RESOLVING_MEMBERS.code);
	}

	public boolean typeResolved() {
		return this.code > 0;
	}

}

/*
    Compiler Core
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
package org.o42a.core.member.field;

import org.o42a.core.ref.Ref;


public class Assignment implements FieldReplacement {

	private final Ref value;

	Assignment(Ref value) {
		this.value = value;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public final MemberField toField() {
		return null;
	}

	@Override
	public final Assignment toAssignment() {
		return this;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "Assignment[" + this.value + ']';
	}

}

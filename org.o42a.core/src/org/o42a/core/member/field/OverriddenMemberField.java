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

import org.o42a.core.member.Member;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.source.LocationInfo;


final class OverriddenMemberField extends MemberField {

	private final MemberField propagatedFrom;

	OverriddenMemberField(
			LocationInfo location,
			MemberOwner owner,
			Field<?> field,
			MemberField propagatedFrom) {
		super(
				location,
				owner,
				field,
				propagatedFrom);
		this.propagatedFrom = propagatedFrom;
	}

	@Override
	public Member getPropagatedFrom() {
		return this.propagatedFrom;
	}

	@Override
	protected Field<?> createField() {
		throw new UnsupportedOperationException();
	}

}

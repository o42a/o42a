/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member;


final class MemberName extends MemberId {

	private final String name;

	MemberName(String name) {
		this.name = name;
	}

	@Override
	public final boolean isValid() {
		return true;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final AdapterId getAdapterId() {
		return null;
	}

	@Override
	public final boolean containsAdapterId() {
		return false;
	}

	@Override
	public final String toName() {
		return this.name;
	}

	@Override
	public final AdapterId toAdapterId() {
		return null;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final MemberName other = (MemberName) obj;

		return this.name.equals(other.name);
	}

	@Override
	public String toString() {
		return this.name;
	}

}

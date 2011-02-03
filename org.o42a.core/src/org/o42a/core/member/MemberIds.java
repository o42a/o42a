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

import java.util.Arrays;

import org.o42a.util.ArrayUtil;


final class MemberIds extends MemberId {

	private final MemberId[] ids;

	MemberIds(MemberId... ids) {
		assert ids.length >= 2 :
			"Too few member identifiers: " + ids.length;
		this.ids = ids;
	}

	@Override
	public boolean isValid() {
		for (MemberId id : this.ids) {
			if (!id.isValid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return lastId().getName();
	}

	@Override
	public AdapterId getAdapterId() {
		return lastId().getAdapterId();
	}

	@Override
	public MemberId[] getIds() {
		return this.ids;
	}

	@Override
	public MemberId getEnclosingId() {
		if (this.ids.length == 2) {
			return this.ids[0];
		}

		final MemberId[] groupIds =
			Arrays.copyOf(this.ids, this.ids.length - 1);

		return new MemberIds(groupIds);
	}

	@Override
	public boolean containsAdapterId() {
		for (int i = this.ids.length - 1; i >= 0; --i) {
			if (this.ids[i].containsAdapterId()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toName() {
		return null;
	}

	@Override
	public AdapterId toAdapterId() {
		return null;
	}

	@Override
	public MemberId[] toIds() {
		return this.ids;
	}

	@Override
	public MemberId append(MemberId memberId) {
		return new MemberIds(ArrayUtil.append(this.ids, memberId));
	}

	@Override
	public boolean startsWith(MemberId other) {

		final MemberId[] ids = other.getIds();

		if (this.ids.length < ids.length) {
			return false;
		}
		for (int i = 0; i < ids.length; ++i) {
			if (!this.ids[i].equals(ids[i])) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.ids);
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

		final MemberIds other = (MemberIds) obj;

		return Arrays.equals(this.ids, other.ids);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		for (MemberId id : this.ids) {
			if (out.length() != 0) {
				out.append(':');
			}
			out.append(id);
		}

		return out.toString();
	}

	private final MemberId lastId() {
		return this.ids[this.ids.length - 1];
	}

}

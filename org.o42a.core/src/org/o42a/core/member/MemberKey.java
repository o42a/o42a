/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.member.MemberId.BROKEN_MEMBER_ID;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.impl.member.MemberFragment;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public class MemberKey implements SubID {

	private static final MemberKey BROKEN_MEMBER_KEY = new BrokenMemberKey();

	public static MemberKey brokenMemberKey() {
		return BROKEN_MEMBER_KEY;
	}

	private final Scope origin;
	private final MemberId memberId;
	private ID id;

	MemberKey(Scope origin, MemberId memberId) {
		assert origin != null :
			"Field declaration scope not specified";
		this.origin = origin;
		this.memberId = memberId;
	}

	private MemberKey() {
		this.origin = null;
		this.memberId = BROKEN_MEMBER_ID;
	}

	public final boolean isValid() {
		return this.memberId.isValid();
	}

	public final Scope getOrigin() {
		return this.origin;
	}

	public final MemberId getMemberId() {
		return this.memberId;
	}

	public final boolean isAdapter() {
		return getAdapterId() != null;
	}

	public final MemberName getMemberName() {
		return getMemberId().getMemberName();
	}

	public final AdapterId getAdapterId() {
		return getMemberId().getAdapterId();
	}

	public final MemberId[] getIds() {
		return getMemberId().getIds();
	}

	/**
	 * Returns a key of enclosing member.
	 *
	 * @return enclosing member key or <code>null</code> if this key is not
	 * a compound one.
	 *
	 * @see MemberId#getEnclosingId()
	 */
	public final MemberKey getEnclosingKey() {

		final MemberId groupId = getMemberId().getEnclosingId();

		if (groupId == null) {
			return null;
		}

		final MemberKey groupKey = new MemberKey(getOrigin(), groupId);

		assert getOrigin().getContainer().member(groupKey) != null :
			"Group not found: " + groupKey;

		return groupKey;
	}

	public final boolean startsWith(MemberKey other) {
		if (other.getOrigin() != getOrigin()) {
			return false;
		}
		return getMemberId().startsWith(other.getMemberId());
	}

	public final MemberKey getLocalKey() {

		final MemberId[] ids = getMemberId().toIds();

		if (ids == null) {
			return this;
		}

		return ids[ids.length - 1].key(getOrigin());
	}

	public final Path toPath() {
		return new MemberFragment(this).toPath();
	}

	@Override
	public final ID toID() {
		if (this.id != null) {
			return this.id;
		}
		return this.id = this.origin.getId().sub(this.memberId);
	}

	@Override
	public final ID toDisplayID() {
		if (this.memberId == null) {
			return null;
		}
		return toID();
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result =
				prime * result
				+ ((this.memberId == null) ? 0 : this.memberId.hashCode());
		result =
				prime * result
				+ ((this.origin == null) ? 0 : this.origin.hashCode());

		return result;
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

		final MemberKey other = (MemberKey) obj;

		if (!this.memberId.equals(other.memberId)) {
			return false;
		}
		if (!this.origin.equals(other.origin)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {

		final ID id = toDisplayID();

		if (id == null) {
			return super.toString();
		}

		return id.toString();
	}

	private static final class BrokenMemberKey extends MemberKey {

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public String toString() {
			return "<broken>";
		}

	}

}

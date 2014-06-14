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

import static org.o42a.core.member.MemberIdKind.FIELD_NAME;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.core.Scope;
import org.o42a.core.member.impl.MemberIds;
import org.o42a.core.member.impl.ReproducedMemberId;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public abstract class MemberId implements SubID {

	public static final MemberId BROKEN_MEMBER_ID = new Broken();

	private static final Scope[] NOT_REPRODUCED = new Scope[0];

	public static final MemberId OWNER_FIELD_ID =
			FIELD_NAME.memberName(CASE_SENSITIVE.name("O"));

	public abstract boolean isValid();

	public abstract MemberName getMemberName();

	public abstract AdapterId getAdapterId();

	public MemberId[] getIds() {
		return new MemberId[] {this};
	}

	/**
	 * Returns an identifier of enclosing member.
	 *
	 * @return enclosing member identifier or <code>null</code> if this
	 * identifier is not a compound.
	 */
	public MemberId getEnclosingId() {
		return null;
	}

	public MemberId getLocalId() {
		return this;
	}

	public abstract boolean containsAdapterId();

	public abstract MemberName toMemberName();

	public abstract AdapterId toAdapterId();

	public MemberId[] toIds() {
		return null;
	}

	public MemberId append(MemberId memberId) {
		assert memberId != null :
			"Member identifier not specified";

		final MemberId[] ids = memberId.toIds();

		if (ids == null) {
			return new MemberIds(this, memberId);
		}

		return new MemberIds(ArrayUtil.prepend(this, ids));
	}

	public Scope[] getReproducedFrom() {
		return NOT_REPRODUCED;
	}

	public boolean startsWith(MemberId other) {
		return equals(other);
	}

	public MemberId reproduceFrom(Scope reproducedFrom) {
		return new ReproducedMemberId(this, reproducedFrom);
	}

	public final MemberKey key(Scope origin) {
		return new MemberKey(origin, this);
	}

	@Override
	public String toString() {

		final ID displayID = toDisplayID();

		if (displayID == null) {
			return super.toString();
		}

		return displayID.toString();
	}

	private static final class Broken extends MemberId {

		private final ID id = ID.id("BROKEN");

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public MemberName getMemberName() {
			return null;
		}

		@Override
		public AdapterId getAdapterId() {
			return null;
		}

		@Override
		public boolean containsAdapterId() {
			return false;
		}

		@Override
		public MemberName toMemberName() {
			return null;
		}

		@Override
		public AdapterId toAdapterId() {
			return null;
		}

		@Override
		public MemberId append(MemberId memberId) {
			return this;
		}

		@Override
		public MemberId reproduceFrom(Scope reproducedFrom) {
			return this;
		}

		@Override
		public ID toID() {
			return this.id;
		}

		@Override
		public ID toDisplayID() {
			return this.id;
		}

		@Override
		public String toString() {
			return "<broken>";
		}

	}

}

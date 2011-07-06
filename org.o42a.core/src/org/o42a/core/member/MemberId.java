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

import org.o42a.core.Scope;


public abstract class MemberId {

	public static final MemberId BROKEN_MEMBER_ID = new Broken();

	private static final Scope[] NOT_REPRODUCED = new Scope[0];

	public static MemberId fieldName(String name) {
		assert name != null :
			"Field name not specified";
		return new MemberName(name);
	}

	public static MemberId clauseName(String name) {
		assert name != null :
			"Clause name not specified";
		return new MemberName('C' + name);
	}

	public static MemberId localName(String name) {
		assert name != null :
			"Local name not specified";
		return new MemberName('L' + name);
	}

	public abstract boolean isValid();


	public abstract String getName();

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

	public abstract boolean containsAdapterId();

	public abstract String toName();

	public abstract AdapterId toAdapterId();

	public MemberId[] toIds() {
		return null;
	}

	public MemberId append(MemberId memberId) {
		assert memberId != null :
			"Member identifier not specified";
		return new MemberIds(this, memberId);
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

	private static final class Broken extends MemberId {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public String getName() {
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
		public String toName() {
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
		public String toString() {
			return "<broken>";
		}

	}

}

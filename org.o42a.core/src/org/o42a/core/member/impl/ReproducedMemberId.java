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
package org.o42a.core.member.impl;

import java.util.Arrays;

import org.o42a.core.Scope;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberName;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.ID;


public final class ReproducedMemberId extends MemberId {

	private final MemberId memberId;
	private final Scope[] reproducedFrom;

	public ReproducedMemberId(MemberId memberId, Scope... reproducedFrom) {
		this.memberId = memberId;
		this.reproducedFrom = reproducedFrom;
	}

	@Override
	public boolean isValid() {
		return this.memberId.isValid();
	}

	@Override
	public MemberName getMemberName() {
		return this.memberId.getMemberName();
	}

	@Override
	public AdapterId getAdapterId() {
		return this.memberId.getAdapterId();
	}

	@Override
	public MemberId[] getIds() {
		return this.memberId.getIds();
	}

	@Override
	public boolean containsAdapterId() {
		return this.memberId.containsAdapterId();
	}

	@Override
	public MemberName toMemberName() {
		return this.memberId.toMemberName();
	}

	@Override
	public AdapterId toAdapterId() {
		return this.memberId.toAdapterId();
	}

	@Override
	public MemberId[] toIds() {
		return this.memberId.toIds();
	}

	@Override
	public MemberId getEnclosingId() {

		final MemberId enclosingId = this.memberId.getEnclosingId();

		if (enclosingId == null) {
			return null;
		}

		return new ReproducedMemberId(enclosingId, this.reproducedFrom);
	}

	@Override
	public MemberId getLocalId() {
		return this.memberId.getLocalId();
	}

	@Override
	public Scope[] getReproducedFrom() {
		return this.reproducedFrom;
	}

	@Override
	public MemberId reproduceFrom(Scope reproducedFrom) {
		return new ReproducedMemberId(
				this.memberId,
				ArrayUtil.append(this.reproducedFrom, reproducedFrom));
	}

	@Override
	public ID toID() {

		ID id = this.memberId.toID();

		for (Scope scope : getReproducedFrom()) {
			id = id.in(scope.getId());
		}

		return id;
	}

	@Override
	public ID toDisplayID() {
		if (this.reproducedFrom == null) {
			return null;
		}
		return toID();
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.memberId.hashCode();
		result = prime * result + Arrays.hashCode(this.reproducedFrom);

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

		final ReproducedMemberId other = (ReproducedMemberId) obj;

		if (!this.memberId.equals(other.memberId)) {
			return false;
		}
		if (!Arrays.equals(this.reproducedFrom, other.reproducedFrom)) {
			return false;
		}

		return true;
	}


}

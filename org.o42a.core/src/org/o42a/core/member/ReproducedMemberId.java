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

import org.o42a.core.Scope;
import org.o42a.util.ArrayUtil;


final class ReproducedMemberId extends MemberId {

	private final MemberId memberId;
	private final Scope[] reproducedFrom;

	ReproducedMemberId(MemberId memberId, Scope... reproducedFrom) {
		this.memberId = memberId;
		this.reproducedFrom = reproducedFrom;
	}

	@Override
	public boolean isValid() {
		return this.memberId.isValid();
	}

	@Override
	public String getName() {
		return this.memberId.getName();
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
	public String toName() {
		return this.memberId.toName();
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

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(this.memberId);
		out.append("[from: ");
		out.append(this.reproducedFrom[0]);

		for (int i = 1; i < this.reproducedFrom.length; ++i) {
			out.append(", ").append(this.reproducedFrom[i]);
		}

		out.append(']');

		return out.toString();
	}

}

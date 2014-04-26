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

import static org.o42a.core.member.MemberKind.*;
import static org.o42a.util.string.Capitalization.CASE_SENSITIVE;

import org.o42a.core.member.clause.ClauseId;
import org.o42a.util.string.ID;
import org.o42a.util.string.Name;


public final class MemberName extends MemberId {

	public static MemberName fieldName(Name name) {
		assert name != null :
			"Field name not specified";
		return new MemberName(FIELD, name);
	}

	public static MemberName tempName(Name name) {
		assert name != null :
			"Temporary field name not specified";
		return new MemberName(TEMP, name);
	}

	public static MemberName clauseId(ClauseId clauseId) {
		assert clauseId != null :
			"Clause identifier not specified";
		assert !clauseId.isName() :
			"Clause name has no identifier";
		return new MemberName(CLAUSE_ID, CASE_SENSITIVE.name(clauseId.name()));
	}

	public static MemberName clauseName(Name name) {
		assert name != null :
			"Clause name not specified";
		return new MemberName(CLAUSE_NAME, name);
	}

	public static MemberName localName(Name name) {
		assert name != null :
			"Local name not specified";
		return new MemberName(LOCAL, name);
	}

	public static MemberName localFieldName(Name name) {
		assert name != null :
			"Local field name not specified";
		return new MemberName(LOCAL_FIELD, name);
	}

	private final MemberKind kind;
	private final Name name;

	private MemberName(MemberKind kind, Name name) {
		this.kind = kind;
		this.name = name;
	}

	@Override
	public final boolean isValid() {
		return true;
	}

	public final MemberKind getKind() {
		return this.kind;
	}

	public final Name getName() {
		return this.name;
	}

	@Override
	public final MemberName getMemberName() {
		return this;
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
	public final MemberName toMemberName() {
		return this;
	}

	@Override
	public final AdapterId toAdapterId() {
		return null;
	}

	@Override
	public ID toID() {
		return getKind().toID(this);
	}

	@Override
	public ID toDisplayID() {
		if (getName() == null) {
			return null;
		}
		return getKind().toDisplayID(this);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + this.kind.hashCode();
		result = prime * result + this.name.hashCode();

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

		final MemberName other = (MemberName) obj;

		if (this.kind != other.kind) {
			return false;
		}

		return this.name.equals(other.name);
	}

}

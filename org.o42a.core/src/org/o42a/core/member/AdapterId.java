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

import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.core.Scope;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.string.ID;


public final class AdapterId extends MemberId {

	public static AdapterId adapterId(TypeRef adapterType) {
		return new AdapterId(adapterType);
	}

	public static AdapterId adapterId(Obj adapterType) {
		return new AdapterId(adapterTypeScope(adapterType));
	}

	public static Scope adapterTypeScope(Obj adapterType) {

		final Field typeField = adapterType.getScope().toField();

		if (typeField == null) {
			return adapterType.getScope();
		}

		return typeField.getOriginal();
	}

	private final TypeRef adapterType;
	private Scope adapterTypeScope;
	private boolean invalid;

	private AdapterId(Scope adapter) {
		this.adapterType = null;
		this.adapterTypeScope = adapter;
	}

	private AdapterId(TypeRef adapterType) {
		this.adapterType = adapterType;
	}

	@Override
	public boolean isValid() {
		getAdapterTypeScope();
		return !this.invalid;
	}

	@Override
	public final MemberName getMemberName() {
		return null;
	}

	@Override
	public final AdapterId getAdapterId() {
		return this;
	}

	@Override
	public boolean containsAdapterId() {
		return true;
	}

	@Override
	public MemberName toMemberName() {
		return null;
	}

	@Override
	public final AdapterId toAdapterId() {
		return this;
	}

	public final Scope getAdapterTypeScope() {
		if (this.adapterTypeScope != null) {
			return this.adapterTypeScope;
		}
		if (this.invalid) {
			return null;
		}
		if (this.adapterType.isValid()) {
			return this.adapterTypeScope =
					adapterTypeScope(this.adapterType.getType());
		}

		this.invalid = true;

		return null;
	}

	public TypeRef adapterType(Scope scope) {
		if (this.adapterType != null) {
			if (this.adapterType.getScope().is(scope)) {
				return this.adapterType;
			}
		}

		final Scope adapterTypeScope = getAdapterTypeScope();

		if (adapterTypeScope == null) {
			return null;
		}

		if (this.adapterType != null) {
			return this.adapterType.upgradeScope(scope);
		}

		final Obj object = adapterTypeScope.toObject();

		return object.selfRef().toStaticTypeRef().rescope(scope);
	}

	@Override
	public ID toID() {
		if (this.invalid) {
			return BROKEN_MEMBER_ID.toID();
		}
		return ID.id().type(getAdapterTypeScope().getId());
	}

	@Override
	public ID toDisplayID() {
		if (this.invalid) {
			return BROKEN_MEMBER_ID.toID();
		}
		if (this.adapterTypeScope != null) {
			return ID.id().type(getAdapterTypeScope().getId());
		}
		if (this.adapterType == null) {
			return null;
		}
		return ID.id().type(
				CASE_INSENSITIVE.name(this.adapterType.toString()));
	}

	@Override
	public int hashCode() {

		final Scope adapterTypeScope = getAdapterTypeScope();

		return adapterTypeScope != null ? adapterTypeScope.hashCode() : 0;
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

		final AdapterId other = (AdapterId) obj;

		return getAdapterTypeScope().is(other.getAdapterTypeScope());
	}

}

/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberKey.brokenMemberKey;

import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public final class FieldKey implements Nesting {

	private static final FieldKey BROKEN_FIELD_KEY =
			new FieldKey(brokenMemberKey(), Visibility.PRIVATE);

	static final FieldKey fieldKey(FieldDeclaration declaration) {
		if (declaration.isOverride()) {
			return overrideField(declaration);
		}
		return declareNewField(declaration);
	}

	private static FieldKey overrideField(FieldDeclaration declaration) {

		final MemberField overridden = overridden(declaration);

		if (overridden != null) {
			return overridden.getFieldKey();
		}

		return BROKEN_FIELD_KEY;
	}

	private static MemberField overridden(FieldDeclaration declaration) {

		final MemberId memberId = declaration.getMemberId();
		final StaticTypeRef declaredInRef = declaration.getDeclaredIn();
		Member overridden = null;

		if (declaredInRef != null) {
			if (!declaredInRef.isValid()) {
				return null;
			}
			overridden = declaredInRef.getType().member(
					memberId,
					Accessor.INHERITANT);
		} else {

			final ObjectType containerType =
					declaration.getContainer().toObject().type();

			for (Sample sample : containerType.getSamples()) {
				overridden = overridden(
						memberId,
						overridden,
						sample.typeObject(dummyUser()));
			}

			final TypeRef ancestor = containerType.getAncestor();

			if (ancestor != null) {
				overridden =
						overridden(memberId, overridden, ancestor.getType());
			}
		}

		if (overridden == null) {
			declaration.getLogger().error(
					"cant_override_unknown",
					declaration,
					"Can not override unknown field '%s'",
					declaration.getDisplayName());
			return null;
		}

		return overridden.toField();
	}

	private static Member overridden(
			MemberId memberId,
			Member overridden,
			Obj ascendant) {
		if (ascendant == null) {
			return overridden;
		}

		final Member member = ascendant.member(memberId, Accessor.INHERITANT);

		if (member == null) {
			return overridden;
		}
		if (overridden == null) {
			return member;
		}
		if (overridden.definedAfter(member)) {
			return overridden;
		}
		if (member.definedAfter(overridden)) {
			return member;
		}

		return overridden;
	}

	private static FieldKey declareNewField(FieldDeclaration declaration) {
		return new FieldKey(
				declaration.getMemberId().key(declaration.getScope()),
				declaration.getVisibility());
	}

	private final MemberKey memberKey;
	private final Visibility visibility;

	private FieldKey(MemberKey memberKey, Visibility visibility) {
		this.memberKey = memberKey;
		this.visibility = visibility;
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	public final Visibility getVisibility() {
		return this.visibility;
	}

	@Override
	public Obj findObjectIn(Scope enclosing) {

		final MemberField field =
				enclosing.getContainer().member(this.memberKey).toField();

		return field.object(dummyUser());
	}

	@Override
	public String toString() {
		if (this.memberKey == null) {
			return super.toString();
		}
		return this.memberKey.toString();
	}

}

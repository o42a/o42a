/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
import static org.o42a.core.member.field.FieldKey.BROKEN_FIELD_KEY;
import static org.o42a.util.fn.Holder.holder;

import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.fn.Holder;


public enum DeclarationMode {

	DECLARE() {

		@Override
		boolean isExplicitOverride(FieldDeclaration declaration) {
			return false;
		}

		@Override
		FieldKey fieldKey(FieldDeclaration declaration) {
			return declaredFieldKey(declaration);
		}

	},

	OVERRIDE() {

		@Override
		boolean isExplicitOverride(FieldDeclaration declaration) {
			return true;
		}

		@Override
		FieldKey fieldKey(FieldDeclaration declaration) {

			final Holder<MemberField> overridden = overriddenField(declaration);

			if (overridden == null) {
				return BROKEN_FIELD_KEY;
			}

			final MemberField overriddenField = overridden.get();

			if (overriddenField != null) {
				return overriddenField.getFieldKey();
			}

			return cantOverride(declaration);
		}

	},

	OVERRIDE_OR_DECLARE() {

		@Override
		boolean isExplicitOverride(FieldDeclaration declaration) {
			return declaration.getDeclaredIn() != null;
		}

		@Override
		FieldKey fieldKey(FieldDeclaration declaration) {

			final Holder<MemberField> overridden = overriddenField(declaration);

			if (overridden == null) {
				return BROKEN_FIELD_KEY;
			}

			final MemberField overriddenField = overridden.get();

			if (overriddenField != null) {
				return overriddenField.getFieldKey();
			}

			return declaredFieldKey(declaration);
		}

	};

	public final boolean canOverride() {
		return this != DECLARE;
	}

	abstract boolean isExplicitOverride(FieldDeclaration declaration);

	abstract FieldKey fieldKey(FieldDeclaration declaration);

	private static FieldKey declaredFieldKey(FieldDeclaration declaration) {
		return new FieldKey(
				declaration.getMemberId().key(declaration.getScope()));
	}

	private static Holder<MemberField> overriddenField(
			FieldDeclaration declaration) {

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
			final Sample sample = containerType.getSample();

			if (sample != null) {
				overridden = overriddenMember(
						memberId,
						overridden,
						sample.getObject());
			}

			final TypeRef ancestor = containerType.getAncestor();

			if (ancestor != null) {
				overridden = overriddenMember(
						memberId,
						overridden,
						ancestor.getType());
			}
		}

		if (overridden == null) {
			return holder(null);
		}
		if (overridden.isStatic()) {
			prohibitedStaticOverride(declaration, overridden);
			return null;
		}

		final MemberField overriddenField = overridden.toField();

		if (overriddenField == null) {
			if (overridden.isAlias()) {
				prohibitedAliasOverride(declaration, overridden);
			} else {
				cantOverride(declaration);
			}
			return null;
		}
		if (overriddenField != overridden) {

			final Field field = overriddenField.field(dummyUser());

			if (!field.getFieldKind().isOrdinal()) {
				prohibitedAliasOverride(declaration, overridden);
				return null;
			}
		}

		return holder(overriddenField);
	}

	private static Member overriddenMember(
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

	private static void prohibitedStaticOverride(
			FieldDeclaration declaration,
			Member overridden) {
		declaration.getLogger().error(
				"prohibited_static_override",
				declaration.getLocation().setDeclaration(overridden),
				"Static field `%s` can not be overridden",
				declaration.getDisplayName());
	}

	private static void prohibitedAliasOverride(
			FieldDeclaration declaration,
			Member overridden) {
		declaration.getLogger().error(
				"prohibited_alias_override",
				declaration.getLocation().setDeclaration(overridden),
				"Alias `%s` can not be overridden",
				declaration.getDisplayName());
	}

	private static FieldKey cantOverride(FieldDeclaration declaration) {
		declaration.getLogger().error(
				"cant_override_unknown",
				declaration,
				"Can not override unknown field '%s'",
				declaration.getDisplayName());
		return BROKEN_FIELD_KEY;
	}

}

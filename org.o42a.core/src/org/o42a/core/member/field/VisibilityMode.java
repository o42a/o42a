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

import org.o42a.core.member.Member;
import org.o42a.core.member.Visibility;


public enum VisibilityMode {

	AUTO_VISIBILITY(Visibility.PUBLIC) {
		@Override
		Visibility byOverridden(MemberField field, MemberField overridden) {
			return overridden.getVisibility();
		}
	},
	PUBLIC_VISIBILITY(Visibility.PUBLIC),
	PROTECTED_VISIBILITY(Visibility.PROTECTED),
	PRIVATE_VISIBILITY(Visibility.PRIVATE);

	private final Visibility defaultVisibility;

	VisibilityMode(Visibility defaultVisibility) {
		this.defaultVisibility = defaultVisibility;
	}

	public final boolean isOverridable() {
		return this.defaultVisibility.isOverridable();
	}

	public final Visibility detectVisibility(MemberField field) {

		final Member[] overridden = field.getOverridden();

		if (overridden.length == 0) {
			return this.defaultVisibility;
		}

		return byOverridden(field, overridden[0].toField());
	}

	Visibility byOverridden(MemberField field, MemberField overridden) {

		final Visibility expected = overridden.getVisibility();

		if (!expected.isOverridable() && !field.isPropagated()) {
			field.getLogger().error(
					"prohibited_private_override",
					field,
					"Private fields can not be overridden");
			return expected;
		}
		if (expected == this.defaultVisibility) {
			return expected;
		}

		field.getLogger().error(
				"unexpected_visibility",
				field.getDeclaration(),
				"Wrong '%s' field visibility: %s, but %s expected",
				field.getDeclaration().getDisplayName(),
				this.defaultVisibility,
				expected);

		return expected;
	}

}

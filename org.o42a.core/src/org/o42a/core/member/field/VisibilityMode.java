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
		Visibility byOverridden(
				Member member,
				FieldDeclaration declaration,
				Member overridden) {
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

	public final Visibility detectVisibility(
			Member member,
			FieldDeclaration declaration) {

		final Member overridden = member.getOverridden();

		if (overridden == null) {
			return this.defaultVisibility;
		}

		return byOverridden(member, declaration, overridden);
	}

	Visibility byOverridden(
			Member member,
			FieldDeclaration declaration,
			Member overridden) {

		final Visibility expected = overridden.getVisibility();

		if (!expected.isOverridable() && !member.isPropagated()) {
			member.getLogger().error(
					"prohibited_private_override",
					member,
					"Private fields can not be overridden");
			return expected;
		}
		if (expected == this.defaultVisibility) {
			return expected;
		}

		member.getLogger().error(
				"unexpected_visibility",
				declaration,
				"Wrong '%s' field visibility: %s, but %s expected",
				declaration.getDisplayName(),
				this.defaultVisibility,
				expected);

		return expected;
	}

}

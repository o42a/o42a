/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.artifact.link.impl.decl;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.member.field.MemberField;


final class DeclaredLink extends Link {

	private final LinkFieldVariant variant;
	private final DeclaredLinkField field;

	DeclaredLink(LinkFieldVariant variant) {
		super(variant.getField(), variant.getField().getArtifactKind());
		this.variant = variant;
		this.field = variant.getLinkField();
	}

	@Override
	public boolean isValid() {
		return this.field.validate(true);
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	@Override
	protected TargetRef buildTargetRef() {
		return this.variant.build(
				this.variant.getDeclaration().getType(),
				null);
	}

	@Override
	protected Link findLinkIn(Scope enclosing) {

		final MemberField field =
				enclosing.getContainer().member(this.field.getKey()).toField();

		return field.field(dummyUser()).getArtifact().toLink();
	}

}

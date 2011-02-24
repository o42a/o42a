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
package org.o42a.core.artifact.link;

import org.o42a.core.member.field.Field;
import org.o42a.core.ref.type.TypeRef;


final class PropagatedLink extends Link {

	private final DeclaredLinkField field;

	PropagatedLink(DeclaredLinkField field) {
		super(field, field.getOverridden()[0].getArtifact());
		this.field = field;
	}

	@Override
	public boolean isValid() {
		return this.field.validate(false);
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	@Override
	protected TypeRef buildTypeRef() {
		return this.field.inheritedTypeRef();
	}

	@Override
	protected TargetRef buildTargetRef() {

		final Field<Link>[] overridden = this.field.getOverridden();

		if (overridden.length != 1) {
			getLogger().requiredLinkTarget(this.field);
		}

		return overridden[0].getArtifact().getTargetRef().upgradeScope(
				getScope().getEnclosingScope());
	}

}

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
package org.o42a.core.artifact.object;

import org.o42a.core.Scope;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.Member;


final class ImplicitSample extends Sample {

	private final StaticTypeRef implicitAscendant;
	private TypeRef ancestor;

	ImplicitSample(Scope scope, StaticTypeRef implicitAscendant) {
		super(implicitAscendant, scope);
		this.implicitAscendant = implicitAscendant;
		assertSameScope(implicitAscendant);
	}

	@Override
	public TypeRef getAncestor() {
		if (this.ancestor != null) {
			return this.ancestor;
		}

		final Obj type = this.implicitAscendant.getType();

		return this.ancestor =
			type.getAncestor().rescope(
					this.implicitAscendant.getRescoper())
			.upgradeScope(getScope());
	}

	@Override
	public StaticTypeRef getTypeRef() {
		return this.implicitAscendant;
	}

	@Override
	public boolean isExplicit() {
		return false;
	}

	@Override
	public Member getOverriddenMember() {
		return null;
	}

	@Override
	public StaticTypeRef getExplicitAscendant() {
		return null;
	}

	@Override
	public String toString() {
		return "ImplicitSample[" + this.implicitAscendant + ']';
	}

	@Override
	protected Obj getObject() {
		return this.implicitAscendant.getType();
	}

}

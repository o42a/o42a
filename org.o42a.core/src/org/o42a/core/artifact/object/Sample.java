/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.artifact.Directive;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;


public abstract class Sample extends Scoped {

	public Sample(LocationSpec location, Scope scope) {
		super(location, scope);
	}

	public abstract TypeRef getAncestor();

	public abstract StaticTypeRef getTypeRef();

	public abstract boolean isExplicit();

	public abstract Member getOverriddenMember();

	public abstract StaticTypeRef getExplicitAscendant();

	public final Obj getType() {
		return getTypeRef().getType();
	}

	public abstract Directive toDirective();

	public final boolean derivedFrom(Sample other) {

		final TypeRef ascendant = getTypeRef();
		final TypeRef otherAscendant = other.getTypeRef();

		return ascendant.derivedFrom(otherAscendant);
	}

	public abstract void inheritMembers(ObjectMembers members);

	public abstract Definitions overrideDefinitions(
			Scope scope,
			Definitions ancestorDefinitions);

}

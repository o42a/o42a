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

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.artifact.Directive;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public abstract class Sample extends Scoped {

	public Sample(LocationInfo location, Scope scope) {
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

	public final Directive toDirective() {
		return getObject().toDirective();
	}

	public final void deriveMembers(ObjectMembers members) {
		members.deriveMembers(getObject());
	}

	public final Definitions overrideDefinitions(
			Scope scope,
			Definitions overriddenDefinitions,
			Definitions ancestorDefinitions) {

		final Obj object = getObject();
		final Definitions definitions = object.overriddenDefinitions(
				scope,
				overriddenDefinitions,
				ancestorDefinitions);

		return object.overrideDefinitions(scope, definitions);
	}

	protected abstract Obj getObject();

}

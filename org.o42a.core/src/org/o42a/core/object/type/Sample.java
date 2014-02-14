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
package org.o42a.core.object.type;

import org.o42a.core.Scoped;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public abstract class Sample extends Scoped implements Derivative {

	private final Ascendants ascendants;

	public Sample(LocationInfo location, Ascendants ascendants) {
		super(location, ascendants.getScope().getEnclosingScope());
		this.ascendants = ascendants;
	}

	public abstract Obj getObject();

	public final Ascendants getAscendants() {
		return this.ascendants;
	}

	public abstract TypeRef getAncestor();

	public abstract TypeRef overriddenAncestor();

	public abstract StaticTypeRef getTypeRef();

	public abstract Member getOverriddenMember();

	public abstract StaticTypeRef getExplicitAscendant();

	@Override
	public final Obj getDerivedObject() {
		return getAscendants().getObject();
	}

	@Override
	public final Sample toSample() {
		return this;
	}

	@Override
	public final Inheritor toInheritor() {
		return null;
	}

	public final void deriveMembers(ObjectMembers members) {
		members.deriveMembers(getObject());
	}

	public final Definitions overrideDefinitions(
			Definitions overriddenDefinitions) {

		final Obj object = getObject();
		final Definitions definitions =
				object.value().overriddenDefinitions(overriddenDefinitions);

		return object.overrideDefinitions(definitions);
	}

	public void resolveAll(FullResolver resolver) {
		getTypeRef().resolveAll(resolver);

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {
			ancestor.resolveAll(resolver);
		}
	}

	public abstract void reproduce(
			Reproducer reproducer,
			AscendantsBuilder<?> ascendants);

}

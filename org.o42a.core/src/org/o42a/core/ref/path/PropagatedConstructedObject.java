/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path;

import static org.o42a.core.object.def.Definitions.emptyDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;


public class PropagatedConstructedObject extends Obj {

	private final ObjectConstructor constructor;
	private final StaticTypeRef propagatedFrom;
	private final TypeRef overriddenAncestor;

	public PropagatedConstructedObject(
			ObjectConstructor constructor,
			Scope scope) {
		this(constructor, scope, constructor.getConstructed());
	}

	private PropagatedConstructedObject(
			ObjectConstructor constructor,
			Scope scope,
			Obj sample) {
		super(
				constructor.distributeIn(scope.getContainer()),
				sample);
		this.constructor = constructor;

		final Ref ref = constructor.toRef();

		this.propagatedFrom = ref.toStaticTypeRef().upgradeScope(scope);
		this.overriddenAncestor =
				ref.rebuildIn(sample.getScope().getEnclosingScope())
				.ancestor(sample.getScope().getEnclosingScope())
				.upgradeScope(scope);
	}

	@Override
	public String toString() {
		return ("Propagated[" + this.propagatedFrom
				+ " / " + getScope().getEnclosingScope() + "]");
	}

	@Override
	protected Nesting createNesting() {
		return this.constructor.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).addImplicitSample(
				this.propagatedFrom,
				this.overriddenAncestor);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

}

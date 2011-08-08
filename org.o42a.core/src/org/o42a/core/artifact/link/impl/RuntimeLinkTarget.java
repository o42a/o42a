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
package org.o42a.core.artifact.link.impl;

import static org.o42a.core.artifact.object.ConstructionMode.RUNTIME_CONSTRUCTION;
import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.util.use.User;


public final class RuntimeLinkTarget extends Obj {

	private final Link link;

	public RuntimeLinkTarget(Link link) {
		super(link, link.distributeIn(link.getScope().getEnclosingContainer()));
		this.link = link;
	}

	@Override
	public Link getMaterializationOf() {
		return this.link;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return RUNTIME_CONSTRUCTION;
	}

	@Override
	public String toString() {
		return this.link.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(this.link.getTypeRef());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();

		final ObjectType targetType =
				this.link.getTypeRef().type(dummyUser());

		if (targetType != null) {
			targetType.wrapBy(type());
		}
	}

	@Override
	protected void fullyResolveDefinitions() {
		super.fullyResolveDefinitions();

		final Obj targetObject =
				this.link.getTypeRef().typeObject(User.dummyUser());

		if (targetObject != null) {
			targetObject.value().wrapBy(value());
		}
	}

}

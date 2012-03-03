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
package org.o42a.core.object.link.impl;

import static org.o42a.core.object.ConstructionMode.STRICT_CONSTRUCTION;

import org.o42a.core.Scope;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.KnownLink;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Resolver;


public class LinkTarget extends Obj {

	private final KnownLink link;

	public LinkTarget(KnownLink link) {
		super(link, link.distributeIn(link.getScope().getContainer()));
		this.link = link;
	}

	@Override
	public final KnownLink getDereferencedLink() {
		return this.link;
	}

	@Override
	public boolean isPropagated() {
		return getWrapped().isPropagated();
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return STRICT_CONSTRUCTION;
	}

	@Override
	public String toString() {
		return this.link.toString();
	}

	@Override
	protected Obj findWrapped() {

		final Resolver resolver =
				getScope().getEnclosingScope().dummyResolver();

		return this.link.getTargetRef().resolve(resolver).materialize();
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
		return null;
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		return this.link.findIn(enclosing).getTarget();
	}

}

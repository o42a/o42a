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
package org.o42a.core.value.link.impl;

import static org.o42a.core.object.ConstructionMode.STRICT_CONSTRUCTION;

import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.link.Link;
import org.o42a.core.value.link.LinkData;


public class LinkTarget extends Obj {

	private final LinkData<?> linkData;

	public LinkTarget(LinkData<?> linkData) {
		super(
				linkData,
				linkData.distributeIn(linkData.getScope().getContainer()));
		this.linkData = linkData;
	}

	@Override
	public final Link getDereferencedLink() {
		return this.linkData.getLink();
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return STRICT_CONSTRUCTION;
	}

	@Override
	protected Nesting createNesting() {
		return getDereferencedLink().getTargetNesting();
	}

	@Override
	protected Obj findWrapped() {

		final Resolver resolver = getScope().getEnclosingScope().resolver();

		return this.linkData.getTargetRef().resolve(resolver).toObject();
	}

	@Override
	protected Ascendants buildAscendants() {

		final TypeRef typeRef = this.linkData.getTypeRef();

		return new Ascendants(this)
		.setAncestor(typeRef)
		.setParameters(
				typeRef.copyParameters()
				.rescope(getScope())
				.toObjectTypeParameters());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

}

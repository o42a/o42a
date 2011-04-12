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

import static org.o42a.core.artifact.object.ConstructionMode.STRICT_CONSTRUCTION;

import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ConstructionMode;
import org.o42a.core.artifact.object.Obj;


class LinkTarget extends ObjectWrap {

	private final Link link;
	private Obj wrapped;

	LinkTarget(Link link) {
		super(link, link.distributeIn(link.getScope().getEnclosingContainer()));
		this.link = link;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return STRICT_CONSTRUCTION;
	}

	@Override
	public Obj getWrapped() {
		if (this.wrapped == null) {
			this.wrapped = this.link.getTargetRef()
			.resolve(getScope().getEnclosingScope())
			.materialize()
			.getWrapped();
		}
		return this.wrapped;
	}

	@Override
	public String toString() {
		return this.link.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(getScope())
		.setAncestor(this.link.getTypeRef());
	}

}

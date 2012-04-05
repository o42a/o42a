/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.DefaultFieldDefinition;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.TypeRef;


public final class PathFieldDefinition extends DefaultFieldDefinition {

	public PathFieldDefinition(BoundPath path, Distributor distributor) {
		super(path, distributor);
	}

	@Override
	public int getLinkDepth() {
		return pathLinkDepth(path());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(path().typeRef(distribute()));
	}

	@Override
	public void defineLink(LinkDefiner definer) {

		final Distributor distributor = distribute();
		final Ref target = path().target(distributor);
		final Obj object = target.getResolution().toObject();
		final Link dereferencedLink = object.getDereferencedLink();
		final TypeRef typeRef;

		if (dereferencedLink == null) {
			typeRef = target.toTypeRef();
		} else {
			typeRef = path().ancestor(this, distribute());
		}

		definer.setTargetRef(target, typeRef);
	}

}

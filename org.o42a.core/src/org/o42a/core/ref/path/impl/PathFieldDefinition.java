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

import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.DefaultFieldDefinition;
import org.o42a.core.ref.type.TypeRef;


public final class PathFieldDefinition extends DefaultFieldDefinition {

	public PathFieldDefinition(Ref ref) {
		super(ref);
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return refDefinitionTarget(getRef());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(getRef().toTypeRef());
	}

	@Override
	public void defineLink(LinkDefiner definer) {

		final Ref target = getRef();
		final Obj object = target.getResolution().toObject();
		final Link dereferencedLink = object.getDereferencedLink();
		final TypeRef typeRef;

		if (dereferencedLink == null) {
			typeRef = target.toTypeRef();
		} else {
			typeRef = target.ancestor(this);
		}

		definer.setTargetRef(target, typeRef);
	}

}

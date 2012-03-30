/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


final class RtLink extends ObjectLink {

	RtLink(LocationInfo location, Scope linkScope) {
		super(location, linkScope.distribute());
	}

	@Override
	public TypeRef getTypeRef() {

		final Obj linkObject = linkObject();

		final LinkValueStruct linkStruct =
				linkObject.value().getValueStruct().toLinkStruct();

		assert linkStruct != null :
			linkObject + " is not a link object";

		return linkStruct.getTypeRef();
	}

	@Override
	public LinkValueType getValueType() {

		final Obj linkObject = linkObject();

		final LinkValueType linkType =
				linkObject.value().getValueType().toLinkType();

		assert linkType != null :
			linkObject + " is not a link object";

		return linkType;
	}

	@Override
	public final boolean isSynthetic() {
		return false;
	}

	@Override
	public final boolean isRuntime() {
		return true;
	}

	@Override
	public void resolveAll(Resolver resolver) {
		getTarget().resolveAll();
	}

	@Override
	protected Obj createTarget() {
		return new RtLinkTarget(this);
	}

	@Override
	protected ObjectLink findLinkIn(Scope enclosing) {
		return new RtLink(this, enclosing);
	}

	private Obj linkObject() {

		final Obj linkObject = getScope().toObject();

		assert linkObject != null :
			linkObject + " is not an object";

		return linkObject;
	}

}

/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.Link;
import org.o42a.core.value.link.LinkValueType;


final class RtLink extends Link {

	private final DereferenceStep dereference;

	RtLink(
			LocationInfo location,
			DereferenceStep dereference,
			Scope linkScope) {
		super(location, linkScope.distribute());
		this.dereference = dereference;
	}

	@Override
	public TypeRef getInterfaceRef() {

		final Obj linkObject = linkObject();
		final TypeParameters<?> linkParameters =
				linkObject.type().getParameters();
		final LinkValueType linkType =
				linkParameters.getValueType().toLinkType();

		assert linkType != null :
			linkObject + " is not a link object";

		return linkType.interfaceRef(linkParameters);
	}

	@Override
	public LinkValueType getValueType() {

		final Obj linkObject = linkObject();

		final LinkValueType linkType =
				linkObject.type().getValueType().toLinkType();

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
	public void resolveAll(FullResolver resolver) {
		getTarget().resolveAll();
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope + "->";
	}

	@Override
	protected Obj createTarget() {
		return new RtLinkTarget(this);
	}

	@Override
	protected RtLink findLinkIn(Scope enclosing) {
		return this.dereference.rtLink(this, enclosing);
	}

	private Obj linkObject() {

		final Obj linkObject = getScope().toObject();

		assert linkObject != null :
			linkObject + " is not an object";

		return linkObject;
	}

}

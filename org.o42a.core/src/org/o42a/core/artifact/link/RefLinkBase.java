/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.Distributor;
import org.o42a.core.def.RefDefBase;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public abstract class RefLinkBase extends RefDefBase {

	protected static TargetRef createTargetRef(Ref ref, TypeRef typeRef) {
		if (typeRef != null) {
			return new TargetRef(
					ref,
					typeRef,
					transparentRescoper(ref.getScope()));
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref),
				transparentRescoper(ref.getScope()));
	}

	protected static TargetRef createTargetRef(
			Ref ref,
			TypeRef typeRef,
			Rescoper rescoper) {
		if (typeRef != null) {
			return new TargetRef(ref, typeRef, rescoper);
		}
		return new TargetRef(
				ref,
				ref.ancestor(ref).rescope(rescoper),
				rescoper);
	}

	public RefLinkBase(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

}

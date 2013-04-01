/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.compiler.ip.ref.AccessRules;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


final class BodyOwner extends Owner implements LocationInfo {

	private final Location location;
	private final LocationInfo bodyRef;

	BodyOwner(
			LocationInfo location,
			LocationInfo bodyRef,
			AccessRules accessRules,
			Ref ownerRef) {
		super(accessRules, ownerRef);
		this.location = location.getLocation();
		this.bodyRef = bodyRef;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	@Override
	public final boolean isBodyReferred() {
		return true;
	}

	@Override
	public Ref targetRef() {
		return ownerRef().getPath()
				.append(new BodyRefFragment(this.bodyRef))
				.target(this.location, distribute());
	}

	@Override
	public Owner body(LocationInfo location, LocationInfo bodyRef) {
		redundantBodyRef(getLogger(), bodyRef.getLocation());
		return this;
	}

	@Override
	public Owner deref(LocationInfo location, LocationInfo deref) {
		return new DerefOwner(location, deref, getAccessRules(), ownerRef());
	}

	@Override
	public Ref bodyRef() {
		redundantBodyRef(getLogger(), getLocation());
		return targetRef();
	}

}

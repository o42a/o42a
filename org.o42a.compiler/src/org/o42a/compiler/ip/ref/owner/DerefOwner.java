/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


final class DerefOwner extends Owner {

	private final LocationInfo location;
	private final LocationInfo deref;

	DerefOwner(
			LocationInfo location,
			LocationInfo deref,
			AccessRules accessRules,
			Ref ownerRef) {
		super(accessRules, ownerRef);
		this.location = location;
		this.deref = deref;
	}

	@Override
	public Ref ref() {
		return ownerRef()
				.getPath()
				.append(new DerefFragment(this.deref))
				.target(this.location, distribute());
	}

	@Override
	public Owner deref(LocationInfo location, LocationInfo deref) {
		return new DerefOwner(
				location,
				deref,
				getAccessRules(),
				ref());
	}

}

/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


class BodyOwner extends Owner {

	private final LocationInfo location;

	BodyOwner(LocationInfo location, Ref ownerRef) {
		super(ownerRef);
		this.location = location;
	}

	@Override
	public Ref targetRef() {
		return this.ownerRef.getPath()
				.append(new BodyRefFragment(this.location))
				.target(this.ownerRef.distribute());
	}

	@Override
	public Owner body(LocationInfo location) {
		redundantBodyRef(this.ownerRef.getLogger(), location);
		return this;
	}

	@Override
	public Ref bodyRef() {
		return body(this.location).targetRef();
	}

}

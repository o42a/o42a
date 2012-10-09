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

import static org.o42a.compiler.ip.ref.owner.MayDereferenceFragment.mayDereference;

import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


final class DefaultOwner extends Owner {

	DefaultOwner(Ref ownerRef) {
		super(ownerRef);
	}

	@Override
	public final boolean isBodyReferred() {
		return false;
	}

	@Override
	public Ref targetRef() {
		return mayDereference(this.ownerRef);
	}

	@Override
	public Owner body(LocationInfo location, LocationInfo bodyRef) {
		return new BodyOwner(location, bodyRef, this.ownerRef);
	}

	@Override
	public Owner deref(LocationInfo location, LocationInfo deref) {
		return new DerefOwner(location, deref, targetRef());
	}

	@Override
	public Ref bodyRef() {
		return this.ownerRef;
	}

}

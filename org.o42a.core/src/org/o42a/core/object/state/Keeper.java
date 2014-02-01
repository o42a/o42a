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
package org.o42a.core.object.state;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Located;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.ID;
import org.o42a.util.string.SubID;


public final class Keeper extends Located implements SubID {

	private final Obj declaredIn;
	private final Ref value;
	private final ID id;
	private Keeper next;

	Keeper(Obj declaredIn, LocationInfo location, Ref value, ID id) {
		super(location);
		this.declaredIn = declaredIn;
		this.id = id;
		this.value = value;
		value.assertSameScope(declaredIn);
	}

	public final Obj getDeclaredIn() {
		return this.declaredIn;
	}

	public final Ref getValue() {
		return this.value;
	}

	@Override
	public final ID toID() {
		return this.id;
	}

	@Override
	public final ID toDisplayID() {
		return this.id;
	}

	public final void resolveAll() {

		final ObjectKeepers keepers = getDeclaredIn().keepers();

		keepers.keeperResolved(this);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	final Keeper getNext() {
		return this.next;
	}

	final void setNext(Keeper next) {
		this.next = next;
	}

}

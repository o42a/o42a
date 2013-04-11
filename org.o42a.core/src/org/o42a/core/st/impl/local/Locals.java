/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.st.impl.local;

import static org.o42a.core.st.sentence.Local.ANONYMOUS_LOCAL_NAME;

import java.util.HashMap;

import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


public final class Locals {

	private final Locals enclosing;
	private Location first;
	private HashMap<Name, Location> locals;

	public Locals(Locals enclosing) {
		this.enclosing = enclosing;
	}

	public Location firstLocal() {
		return this.first;
	}

	public boolean declareLocal(LocationInfo location, Name name) {
		if (duplicateLocal(location, name)) {
			return false;
		}
		addLocal(location, name);
		return true;
	}

	private boolean duplicateLocal(LocationInfo location, Name name) {

		final Location existing = existingLocal(name);

		if (existing == null) {
			return false;
		}

		location.getLocation().getLogger().error(
				"duplicate_local",
				location.getLocation().addAnother(existing),
				"Imperative block with name '%s' already declared",
				name);

		return true;
	}

	private Location existingLocal(Name name) {
		if (name.is(ANONYMOUS_LOCAL_NAME)) {
			// Anonymous locals allowed in enclosing blocks.
			return blockAnonymousLocal();
		}
		return localByName(name);
	}

	private Location localByName(Name name) {
		if (this.locals != null) {

			final Location found = this.locals.get(name);

			if (found != null) {
				return found;
			}
		}
		if (this.enclosing == null) {
			return null;
		}
		return this.enclosing.localByName(name);
	}

	private Location blockAnonymousLocal() {
		if (this.locals == null) {
			return null;
		}
		return this.locals.get(ANONYMOUS_LOCAL_NAME);
	}

	private void addLocal(LocationInfo location, Name name) {
		if (this.locals == null) {
			this.first = location.getLocation();
			this.locals = new HashMap<>();
		}
		this.locals.put(name, location.getLocation());
	}

}

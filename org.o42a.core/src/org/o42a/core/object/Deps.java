/*
    Compiler Core
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
package org.o42a.core.object;

import static org.o42a.util.collect.Iterators.readonlyIterator;

import java.util.LinkedHashMap;

import org.o42a.core.object.state.Dep;
import org.o42a.core.object.state.ObjectDeps;
import org.o42a.core.ref.Ref;
import org.o42a.util.collect.ReadonlyIterable;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.string.ID;


public final class Deps extends ObjectDeps implements ReadonlyIterable<Dep> {

	private static final ID DEP_PREFIX =
			ID.id(ID.id("D").setDescription("Dependency #"));

	private LinkedHashMap<Object, Dep> deps = new LinkedHashMap<>();
	private int depNameSeq;

	Deps(Obj object) {
		super(object);
	}

	@Override
	public ReadonlyIterator<Dep> iterator() {
		return readonlyIterator(this.deps.values().iterator());
	}

	public Dep addDep(Ref ref) {
		assert getObject().getContext().fullResolution().assertIncomplete();

		final int newDepId = this.depNameSeq + 1;
		final Dep newDep = newDep(
				ref,
				DEP_PREFIX.suffix(Integer.toString(newDepId)));
		final Dep dep = addDep(newDep);

		if (dep != newDep) {
			// The old dependency reused.
			return dep;
		}
		// The new dependency added.
		this.depNameSeq = newDepId;

		return dep;
	}

	@Override
	protected void depResolved(Dep dep) {

		final LinkUses linkUses = getObject().type().linkUses();

		if (linkUses != null) {
			linkUses.depAdded();
		}
	}

	private Dep addDep(Dep dep) {

		final Object key = dep.getDepKey();
		final Dep found = this.deps.put(key, dep);

		if (found == null) {
			return dep;
		}

		this.deps.put(key, found);
		reuseDep(found);

		return found;
	}

}

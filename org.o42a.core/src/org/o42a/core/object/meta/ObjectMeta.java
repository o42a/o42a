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
package org.o42a.core.object.meta;

import java.util.IdentityHashMap;
import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;


public abstract class ObjectMeta {

	private IdentityHashMap<MetaDep, Boolean> tripped;
	private MetaDeps deps;
	private Iterator<Scope> checkedAscendants;
	private MetaDep checkedDep;
	private boolean initialized;
	private byte updated;

	public boolean isUpdated() {
		if (this.updated != 0) {
			return this.updated > 0;
		}
		if (!meta().getObject().isClone()) {
			this.updated = 1;
			return true;
		}
		if (hasUpdates()) {
			this.updated = 1;
			return true;
		}
		this.updated = -1;
		return false;
	}

	final void addDep(MetaDep dep) {

		MetaDep currentDep = dep;
		Meta currentMeta = meta();

		for (;;) {

			final ObjectMeta objectMeta = currentMeta;

			if (objectMeta.deps == null) {
				objectMeta.deps = new MetaDeps();
			}
			objectMeta.deps.add(currentDep);

			final MetaDep parentDep = currentDep.parentDep();

			if (parentDep == null) {
				return;
			}

			currentMeta = currentDep.parentMeta(currentMeta);
			currentDep = parentDep;
		}
	}

	boolean checkUpdated(MetaDep dep) {

		final Meta meta = meta();

		if (this.tripped == null) {
			this.tripped = new IdentityHashMap<MetaDep, Boolean>();
		} else {

			final Boolean cached = this.tripped.get(dep);

			if (cached != null) {
				return cached.booleanValue();
			}
		}

		final boolean triggered = dep.triggered(meta);

		if (triggered && dep.changed(meta)) {
			this.tripped.put(dep, Boolean.TRUE);
			return true;
		}

		this.tripped.put(dep, Boolean.FALSE);

		return false;
	}

	private void init() {
		if (this.initialized) {
			return;
		}

		this.initialized = true;

		final ObjectMeta parentMeta = meta().getParentMeta();

		if (parentMeta != null) {
			parentMeta.init();
			importParentDeps(parentMeta);
		}
	}

	private void importParentDeps(ObjectMeta parentMeta) {
		if (parentMeta.deps == null) {
			return;
		}

		for (MetaDep parentDep = parentMeta.deps.getFirst();
				parentDep != null;
				parentDep = parentDep.getNext()) {

			final MetaDep dep = parentDep.nestedDep();

			if (dep == null) {
				continue;
			}
			if (!parentDep.nestedMeta(parentMeta.meta()).is(meta())) {
				continue;
			}
			if (this.deps == null) {
				this.deps = new MetaDeps();
			} else if (this.deps.contains(dep)) {
				continue;
			}
			this.deps.add(dep);
		}
	}

	private final Meta meta() {
		return (Meta) this;
	}

	private boolean hasUpdates() {

		boolean hasUpdates = false;
		final Meta meta = meta();
		final Obj object = meta.getObject();

		if (this.checkedAscendants == null) {
			this.checkedAscendants =
					object.type().allAscendants().keySet().iterator();
		}

		for (;;) {

			final MetaDep dep = nextDep();

			if (dep == null) {
				this.checkedAscendants = null;
				return hasUpdates;
			}
			if (dep.updated(meta)) {
				hasUpdates = true;
			}
		}
	}

	private MetaDep nextDep() {
		for (;;) {
			if (this.checkedDep != null) {
				this.checkedDep = this.checkedDep.getNext();
				if (this.checkedDep != null) {
					return this.checkedDep;
				}
			}

			final ObjectMeta nextMeta = nextMetaWithDeps();

			if (nextMeta == null) {
				return null;
			}

			return this.checkedDep = nextMeta.deps.getFirst();
		}
	}

	private ObjectMeta nextMetaWithDeps() {
		while (this.checkedAscendants.hasNext()) {

			final Scope nextAscendant = this.checkedAscendants.next();

			if (nextAscendant.is(meta().getObject().getScope())) {
				// No need to check an explicit dependencies.
				continue;
			}

			final ObjectMeta nextMeta = nextAscendant.toObject().meta();

			nextMeta.init();
			if (nextMeta.deps != null && !nextMeta.deps.isEmpty()) {
				return nextMeta;
			}
		}

		return null;
	}

}

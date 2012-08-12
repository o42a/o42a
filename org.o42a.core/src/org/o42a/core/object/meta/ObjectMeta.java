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

import java.util.*;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;


public abstract class ObjectMeta {

	private IdentityHashMap<MetaKey, Boolean> tripped;
	private HashMap<MetaKey, MetaDep> deps;
	private Iterator<Scope> checkedAscendants;
	private Iterator<MetaDep> checkedDeps;
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

		final MetaKey key = dep.getKey();

		MetaDep currentDep = dep;
		Meta currentMeta = meta();

		for (;;) {

			final ObjectMeta objectMeta = currentMeta;

			if (objectMeta.deps == null) {
				objectMeta.deps = new HashMap<MetaKey, MetaDep>();
			}
			objectMeta.deps.put(key, currentDep);

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
		final MetaKey key = dep.getKey();

		if (this.tripped == null) {
			this.tripped = new IdentityHashMap<MetaKey, Boolean>();
		} else {

			final Boolean cached = this.tripped.get(key);

			if (cached != null) {
				return cached.booleanValue();
			}
		}

		final boolean triggered = dep.triggered(meta);

		if (triggered && dep.changed(meta)) {
			this.tripped.put(key, Boolean.TRUE);
			return true;
		}

		this.tripped.put(key, Boolean.FALSE);

		return false;
	}

	private ObjectMeta init() {

		final ObjectMeta parentMeta = meta().getParentMeta();

		if (this.initialized) {
			return this;
		}
		this.initialized = true;

		if (parentMeta != null) {
			parentMeta.init();
			importParentDeps(parentMeta);
		}

		return this;
	}

	private void importParentDeps(ObjectMeta parentMeta) {
		if (parentMeta.deps == null) {
			return;
		}

		for (Map.Entry<MetaKey, MetaDep> e : parentMeta.deps.entrySet()) {

			final MetaDep parentDep = e.getValue();
			final MetaKey key = e.getKey();

			if (this.deps != null && this.deps.containsKey(key)) {
				continue;
			}

			final MetaDep dep = parentDep.nestedDep();

			if (dep == null) {
				continue;
			}
			if (!parentDep.nestedMeta(parentMeta.meta()).is(meta())) {
				continue;
			}
			if (this.deps == null) {
				this.deps = new HashMap<MetaKey, MetaDep>();
			}
			this.deps.put(key, dep);
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
			if (this.checkedDeps != null) {
				if (this.checkedDeps.hasNext()) {
					return this.checkedDeps.next();
				}
				this.checkedDeps = null;
			}

			final ObjectMeta nextMeta = nextMeta();

			if (nextMeta == null) {
				return null;
			}
			if (nextMeta.deps == null) {
				continue;
			}

			this.checkedDeps = nextMeta.deps.values().iterator();
		}
	}

	private ObjectMeta nextMeta() {
		while (this.checkedAscendants.hasNext()) {

			final Scope nextAscendant = this.checkedAscendants.next();

			if (nextAscendant.is(meta().getObject().getScope())) {
				// No need to check an explicit dependencies.
				continue;
			}

			final ObjectMeta nextMeta = nextAscendant.toObject().meta();

			return nextMeta.init();
		}

		return null;
	}

}

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

	protected static MetaDep createDep(MetaTrigger trigger, MetaUpdate update) {
		return new MetaDep(trigger, update);
	}

	private IdentityHashMap<MetaTrigger, Boolean> tripped;
	private MetaDeps deps;
	private Iterator<Scope> checkedAscendants;
	private MetaDep checkedDep;
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

	protected void addDep(MetaDep dep) {
		if (this.deps == null) {
			this.deps = new MetaDeps();
		}
		this.deps.add(dep);
	}

	int triggerIsTripped(MetaTrigger trigger, Meta meta) {
		if (this.tripped == null) {
			this.tripped = new IdentityHashMap<MetaTrigger, Boolean>();
		} else {

			final Boolean cached = this.tripped.get(trigger);

			if (cached != null) {
				return cached.booleanValue() ? 1 : 0;
			}
		}

		final boolean tripped = trigger.tripped(meta);

		this.tripped.put(trigger, Boolean.valueOf(tripped));

		return tripped ? 2 : 0;
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
			if (dep.update(meta)) {
				hasUpdates = true;
			}
		}
	}

	private MetaDep nextDep() {
		if (this.checkedDep != null) {
			this.checkedDep = this.checkedDep.getNext();
			if (this.checkedDep != null) {
				return this.checkedDep;
			}
		}

		for (;;) {

			final ObjectMeta nextMeta = nextMeta();

			if (nextMeta == null) {
				return null;
			}
			if (nextMeta.deps == null) {
				continue;
			}

			this.checkedDep = nextMeta.deps.getFirst();
			if (this.checkedDep != null) {
				return this.checkedDep;
			}
		}
	}

	private ObjectMeta nextMeta() {
		while (this.checkedAscendants.hasNext()) {

			final Scope nextAscendant = this.checkedAscendants.next();

			if (nextAscendant.is(meta().getObject().getScope())) {
				// No need to check an explicit dependencies.
				continue;
			}

			return nextAscendant.toObject().meta();
		}

		return null;
	}

}

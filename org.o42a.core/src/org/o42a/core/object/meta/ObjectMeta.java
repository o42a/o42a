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

import org.o42a.core.object.Meta;


public abstract class ObjectMeta {

	protected static MetaDep createDep(MetaTrigger trigger, MetaUpdate update) {
		return new MetaDep(trigger, update);
	}

	private IdentityHashMap<MetaTrigger, Boolean> tripped;
	private MetaDeps deps;
	private MetaDep checked;
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

	boolean triggerIsTripped(MetaTrigger trigger, Meta meta) {
		if (this.tripped == null) {
			this.tripped = new IdentityHashMap<MetaTrigger, Boolean>();
		} else {

			final Boolean cached = this.tripped.get(trigger);

			if (cached != null) {
				return cached.booleanValue();
			}
		}

		final boolean tripped = trigger.tripped(meta);

		this.tripped.put(trigger, Boolean.valueOf(tripped));

		return tripped;
	}

	private final Meta meta() {
		return (Meta) this;
	}

	private boolean hasUpdates() {
		if (this.deps == null) {
			return false;
		}

		final Meta meta = meta();

		if (this.checked == null) {
			this.checked = this.deps.getFirst();
		} else {
			this.checked = this.checked.getNext();
		}

		for (;;) {
			if (this.checked == null) {
				return false;
			}
			if (this.checked.updated(meta)) {
				this.checked = null;
				return true;
			}
			this.checked = this.checked.getNext();
		}
	}

}

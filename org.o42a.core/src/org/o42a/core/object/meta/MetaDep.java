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

import org.o42a.core.object.Meta;


public final class MetaDep {

	private final MetaTrigger trigger;
	private final MetaUpdate update;
	private MetaDep next;

	MetaDep(MetaTrigger trigger, MetaUpdate update) {
		assert trigger != null :
			"Meta trigger not specified";
		assert update != null :
			"Meta update not specified";
		this.trigger = trigger;
		this.update = update;
	}

	public final MetaTrigger getTrigger() {
		return this.trigger;
	}

	public final MetaUpdate getUpdate() {
		return this.update;
	}

	public boolean update(Meta meta) {

		final ObjectMeta top = topMeta(meta);

		if (top == null) {
			return false;
		}

		final UpdatedMeta updated = updatedMeta(meta);
		final int triggerIsTripped =
				top.triggerIsTripped(getTrigger(), updated.getMeta());

		switch (triggerIsTripped) {
		case 0:
			return false;
		case 1:
			return true;
		}

		return updated.update();
	}

	@Override
	public String toString() {
		if (this.update == null) {
			return super.toString();
		}
		return "MetaDep[update " + this.update + " on " + this.trigger +']';
	}

	final MetaDep getNext() {
		return this.next;
	}

	final void setNext(MetaDep next) {
		this.next = next;
	}

	private Meta topMeta(Meta meta) {

		Meta currentMeta = meta;
		MetaUpdate currentUpdate = getUpdate();

		for (;;) {

			final MetaUpdate parentUpdate = currentUpdate.parentUpdate();

			if (parentUpdate == null) {
				return currentMeta;
			}

			final Meta parentMeta = currentUpdate.parentMeta(currentMeta);

			if (parentMeta == null) {
				// Out of the scope. Trigger can no longer trip.
				return null;
			}

			currentMeta = parentMeta;
			currentUpdate = parentUpdate;
		}
	}

	private UpdatedMeta updatedMeta(Meta meta) {

		Meta currentMeta = meta;
		MetaUpdate currentUpdate = getUpdate();

		for (;;) {

			final MetaUpdate nestedUpdate = currentUpdate.nestedUpdate();

			if (nestedUpdate == null) {
				return new UpdatedMeta(currentMeta, currentUpdate);
			}

			currentMeta = currentUpdate.nestedMeta(currentMeta);
			currentUpdate = nestedUpdate;
		}
	}

	private static final class UpdatedMeta {

		private final Meta meta;
		private final MetaUpdate update;

		UpdatedMeta(Meta meta, MetaUpdate update) {
			this.meta = meta;
			this.update = update;
		}

		public final Meta getMeta() {
			return this.meta;
		}

		public final boolean update() {
			return this.update.update(getMeta());
		}

	}

}

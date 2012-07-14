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

	public boolean updated(Meta meta) {

		final Meta top = topMeta(meta);

		if (top == null) {
			return false;
		}

		final ObjectMeta objectMeta = top;

		return objectMeta.triggerIsTripped(getTrigger(), innermostMeta(meta));
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
				// Put of the scope. Trigger can no longer trip.
				return null;
			}

			currentMeta = parentMeta;
			currentUpdate = parentUpdate;
		}
	}

	private Meta innermostMeta(Meta meta) {

		Meta currentMeta = meta;
		MetaUpdate currentUpdate = getUpdate();

		for (;;) {

			final MetaUpdate nestedUpdate = currentUpdate.nestedUpdate();

			if (nestedUpdate == null) {
				return currentMeta;
			}

			currentMeta = currentUpdate.nestedMeta(currentMeta);
			currentUpdate = nestedUpdate;
		}
	}

}

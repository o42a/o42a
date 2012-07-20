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


public abstract class MetaDep {

	public abstract MetaKey getKey();

	public boolean update(Meta meta) {

		final UpdatedMeta top = topMeta(meta);

		if (top == null) {
			return false;
		}

		return top.checkUpdated();
	}

	public abstract MetaDep parentDep();

	public abstract MetaDep nestedDep();

	public abstract Meta parentMeta(Meta meta);

	public abstract Meta nestedMeta(Meta meta);

	protected abstract boolean triggered(Meta meta);

	protected abstract boolean updateMeta(Meta meta);

	private UpdatedMeta topMeta(Meta meta) {

		Meta currentMeta = meta;
		MetaDep currentDep = this;

		for (;;) {

			final MetaDep parentDep = currentDep.parentDep();

			if (parentDep == null) {
				return new UpdatedMeta(currentMeta, currentDep);
			}

			final Meta parentMeta = currentDep.parentMeta(currentMeta);

			if (parentMeta == null) {
				// Out of the scope. Trigger can no longer trip.
				return null;
			}

			currentMeta = parentMeta;
			currentDep = parentDep;
		}
	}

	private static final class UpdatedMeta {

		private final Meta meta;
		private final MetaDep dep;

		UpdatedMeta(Meta meta, MetaDep dep) {
			this.meta = meta;
			this.dep = dep;
		}

		public final boolean checkUpdated() {

			final ObjectMeta meta = this.meta;

			return meta.checkUpdated(this.dep);
		}

	}

}

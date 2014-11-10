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
package org.o42a.core.object.meta;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMeta;
import org.o42a.util.collect.Chain;


final class MetaUpdatesChecker {

	private final ObjectMeta meta;
	private Iterator<Scope> checkedAscendants;
	private MetaDep checkedDep;

	MetaUpdatesChecker(ObjectMeta meta) {
		this.meta = meta;
	}

	boolean hasUpdates() {

		boolean hasUpdates = false;
		final Obj object = this.meta.getObject();

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
			if (dep.updated(this.meta)) {
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

			final ObjectMetaBase nextMeta = nextMetaWithDeps();

			if (nextMeta == null) {
				return null;
			}

			return this.checkedDep = nextMeta.deps().getFirst();
		}
	}

	private ObjectMetaBase nextMetaWithDeps() {
		while (this.checkedAscendants.hasNext()) {

			final Scope nextAscendant = this.checkedAscendants.next();

			if (nextAscendant.is(this.meta.getObject().getScope())) {
				// No need to check an explicit dependencies.
				continue;
			}

			final ObjectMetaBase nextMeta = nextAscendant.toObject().meta();

			nextMeta.initialize();

			final Chain<MetaDep> nextDeps = nextMeta.deps();

			if (nextDeps != null && !nextDeps.isEmpty()) {
				return nextMeta;
			}
		}

		return null;
	}

}

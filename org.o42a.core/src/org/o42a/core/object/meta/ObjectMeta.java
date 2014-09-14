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

import static org.o42a.analysis.use.User.dummyUser;

import java.util.IdentityHashMap;

import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Meta;
import org.o42a.util.Chain;


public abstract class ObjectMeta {

	private IdentityHashMap<MetaDep, Boolean> tripped;
	private Chain<MetaDep> deps;
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

	final Chain<MetaDep> deps() {
		return this.deps;
	}

	final void addDep(MetaDep dep) {

		MetaDep currentDep = dep;
		Meta currentMeta = meta();

		for (;;) {

			final ObjectMeta objectMeta = currentMeta;

			if (objectMeta.deps == null) {
				objectMeta.deps = newDeps();
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
			this.tripped = new IdentityHashMap<>();
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

	void init() {
		if (this.initialized) {
			return;
		}
		this.initialized = true;
		importParentDeps();
		initTypeParameters();
		initNestedDeps();
	}

	private void importParentDeps() {

		final ObjectMeta parentMeta = meta().getParentMeta();

		if (parentMeta == null) {
			return;
		}
		parentMeta.init();
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
				this.deps = newDeps();
			} else if (this.deps.contains(dep)) {
				continue;
			}
			this.deps.add(dep);
		}
	}

	private void initTypeParameters() {
		meta().getObject().type().getParameters().validateAll();
	}

	private void initNestedDeps() {
		for (Member member : meta().getObject().getMembers()) {
			if (member.isClone()) {
				// Clone can not provide new dependencies.
				continue;
			}

			final MemberField memberField = member.toField();

			if (memberField == null) {
				// Only fields are interesting.
				continue;
			}

			final Field field = memberField.field(dummyUser());

			if (field.getFieldKind().isOwner()) {
				// Only nested fields are interesting.
				continue;
			}

			final ObjectMeta nestedMeta = field.toObject().meta();

			nestedMeta.initTypeParameters();
			nestedMeta.initNestedDeps();
		}
	}

	private boolean hasUpdates() {
		return new MetaUpdatesChecker(meta()).hasUpdates();
	}

	private final Meta meta() {
		return (Meta) this;
	}

	private static Chain<MetaDep> newDeps() {
		return new Chain<>(MetaDep::getNext, MetaDep::setNext);
	}

}

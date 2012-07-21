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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;


public abstract class MetaDep {

	private final MetaKey key;
	private final Meta declaredIn;
	private BoundPath parentPath;

	public MetaDep(Meta declaredIn, MetaKey key) {
		assert declaredIn != null :
			"Meta the dependency declared in is not specified";
		assert key != null :
			"Meta key not specified";
		this.declaredIn = declaredIn;
		this.key = key;
	}

	public final MetaKey getKey() {
		return this.key;
	}

	public final Meta getDeclaredIn() {
		return this.declaredIn;
	}

	public boolean update(Meta meta) {

		final UpdatedMeta top = topMeta(meta);

		if (top == null) {
			return false;
		}

		return top.checkUpdated();
	}

	public abstract MetaDep parentDep();

	public abstract MetaDep nestedDep();

	public final Meta parentMeta(Meta meta) {

		final BoundPath parentPath = parentPath();

		if (parentPath == null) {
			return null;
		}

		meta.getObject().assertDerivedFrom(getDeclaredIn().getObject());

		final PathResolution parentResolution = parentPath.resolve(
				pathResolver(meta.getObject().getScope(), dummyUser()));
		final Meta parentMeta = parentResolution.getObject().meta();

		if (!meta.getParentMeta().is(parentMeta)) {
			// Out of scope.
			return null;
		}

		return parentMeta;
	}

	public abstract Meta nestedMeta(Meta meta);

	@Override
	public String toString() {
		if (this.key == null) {
			return super.toString();
		}
		return this.key.toString();
	}

	protected abstract boolean triggered(Meta meta);

	protected abstract boolean updateMeta(Meta meta);

	private final BoundPath parentPath() {
		if (this.parentPath != null) {
			return this.parentPath;
		}

		final MetaDep parentDep = parentDep();

		if (parentDep == null) {
			return null;
		}

		final Scope scope = getDeclaredIn().getObject().getScope();
		final Scope enclosingScope = scope.getEnclosingScope();
		final Obj enclosingObject = enclosingScope.toObject();

		if (enclosingObject != null) {
			return scope.getEnclosingScopePath().bind(scope, scope);
		}

		assert enclosingScope.toMember() != null :
			"Wrong enclosing scope: " + enclosingScope;

		return scope.getEnclosingScopePath()
				.append(enclosingScope.getEnclosingScopePath())
				.bind(scope, scope);
	}

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

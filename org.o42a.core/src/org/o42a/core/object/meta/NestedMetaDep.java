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


public abstract class NestedMetaDep extends MetaDep {

	private final MetaDep parent;
	private final BoundPath parentPath;

	public NestedMetaDep(MetaDep parent, Meta declaredIn) {
		super(declaredIn, parent.getKey());
		assert parent.getDeclaredIn().is(declaredIn.getParentMeta()) :
			parent.getDeclaredIn() + " is not a parent of " + declaredIn;
		this.parent = parent;
		this.parentPath = parentPath();
	}

	@Override
	public final MetaDep parentDep() {
		return this.parent;
	}

	@Override
	public final Meta parentMeta(Meta meta) {
		meta.getObject().assertDerivedFrom(getDeclaredIn().getObject());

		final PathResolution parentResolution = this.parentPath.resolve(
				pathResolver(meta.getObject().getScope(), dummyUser()));
		final Meta parentMeta = parentResolution.getObject().meta();

		if (!meta.getParentMeta().is(parentMeta)) {
			// Out of scope.
			return null;
		}

		return parentMeta;
	}

	@Override
	protected boolean triggered(Meta meta) {
		return parentDep().triggered(parentMeta(meta));
	}

	@Override
	protected boolean updateMeta(Meta meta) {
		return parentDep().updateMeta(parentMeta(meta));
	}

	private final BoundPath parentPath() {

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

}

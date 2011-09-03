/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.member.impl.local;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;
import org.o42a.core.member.local.DepKind;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class EnclosingOwnerDep extends Dep {

	private Obj target;

	public EnclosingOwnerDep(Obj object) {
		super(object, DepKind.ENCLOSING_OWNER_DEP);
		assert object != null :
			"Dependency object not specified";

		final LocalScope local =
				object.getScope().getEnclosingContainer().toLocal();

		assert local != null :
			object + " is not a local object";

		this.target = local.getOwner();
	}

	@Override
	public Object getKey() {
		return null;
	}

	@Override
	public final Artifact<?> getTarget() {
		return this.target;
	}

	@Override
	public final Field<?> getDepField() {
		return null;
	}

	@Override
	public final Ref getDepRef() {
		return null;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(new EnclosingOwnerDep(scope.toObject()).toPath());
	}

	@Override
	public String toString() {
		if (this.target == null) {
			return super.toString();
		}
		return "Dep[<owner> of " + getObject() + ']';
	}

	@Override
	protected Container resolveDep(
			PathResolver resolver,
			Path path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker) {

		final Obj owner = enclosingLocal.getOwner();

		walker.up(object, this, owner);

		return owner;
	}

}

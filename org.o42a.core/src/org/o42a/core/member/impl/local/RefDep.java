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
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class RefDep extends Dep {

	private final String name;
	private final Ref depRef;
	private final Artifact<?> target;

	public RefDep(Obj object, Ref depRef, String name) {
		super(object, DepKind.REF_DEP);
		this.depRef = depRef;
		this.name = name;

		final Container container =
				object.getScope().getEnclosingContainer();
		final LocalScope local = container.toLocal();

		assert local != null :
			object + " is not a local object";

		this.target = this.depRef.resolve(
				local.newResolver(dummyUser())).toArtifact();
	}

	@Override
	public Object getKey() {
		return this.depRef;
	}

	@Override
	public String getName() {
		return this.name;
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
		return this.depRef;
	}

	@Override
	public String toString() {
		if (this.depRef == null) {
			return super.toString();
		}
		return "Dep[" + this.depRef + " of " + getObject() + ']';
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(
				new RefDep(scope.toObject(), getDepRef(), this.name).toPath());
	}

	@Override
	protected Container resolveDep(
			PathResolver resolver,
			Path path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker) {

		final LocalResolver localResolver =
				enclosingLocal.newResolver(resolver);

		if (resolver.isFullResolution()) {
			if (resolver.isValueResolution()
					&& index == path.getFragments().length - 1) {
				// Resolve only the last value.
				this.depRef.resolveValues(localResolver);
			} else {
				this.depRef.resolveAll(localResolver);
			}
		}

		final Resolution resolution = this.depRef.resolve(localResolver);

		walker.refDep(object, this, this.depRef);

		return resolution.toContainer();
	}

}

/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.local;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.core.Container;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.*;
import org.o42a.core.st.Reproducer;


public final class Dep extends PathFragment {

	/**
	 * Builds dependency on the given enclosing local scope's field.
	 *
	 * @param object local object this dependency is built for.
	 * @param dependency local field key the <code>object</code> depends on.
	 *
	 * @return new field dependency.
	 */
	public static Dep fieldDep(Obj object, MemberKey dependency) {
		return new Dep(object, dependency);
	}

	/**
	 * Builds dependency on the {@link LocalScope#getOwner() owner} of enclosing
	 * local scope.
	 *
	 * @param object local object this dependency is built for.
	 *
	 * @return new dependency on enclosing scope's owner.
	 */
	public static Dep enclosingOwnerDep(Obj object) {
		return new Dep(object);
	}

	private final Obj object;
	private final Field<?> dependency;
	private final Artifact<?> target;

	private Dep(Obj object, MemberKey dependencyKey) {
		assert object != null :
			"Object not specified";
		this.object = object;

		final Container container =
			object.getScope().getEnclosingContainer();

		assert container.toLocal() != null :
			object + " is not a local object";

		this.dependency = container.member(dependencyKey).toField();

		assert this.dependency != null :
			"Dependency " + dependencyKey + " of " + object + " not found";

		this.target = this.dependency.getArtifact();
	}

	private Dep(Obj object) {

		final LocalScope local =
			object.getScope().getEnclosingContainer().toLocal();

		assert local != null :
			object + " is not a local object";

		this.object = object;
		this.dependency = null;
		this.target = local.getOwner();
	}

	public final Obj getObject() {
		return this.object;
	}

	public final Artifact<?> getTarget() {
		return this.target;
	}

	public final Field<?> getDependency() {
		return this.dependency;
	}

	public final boolean dependencyOnEnclosingOwner() {
		return this.dependency == null;
	}

	@Override
	public Container resolve(
			LocationInfo location,
			Path path,
			int index,
			Scope start,
			PathWalker walker) {

		final Obj object = start.getContainer().toObject();

		assert object != null :
			"Dependency " + path.toString(index + 1)
			+ " should be resolved against object, but were not: " + start;

		final LocalScope enclosingLocal =
			object.getScope().getEnclosingContainer().toLocal();

		assert enclosingLocal != null :
			object + " is inside " + object.getScope().getEnclosingContainer()
			+ ", which is not a local scope";

		if (dependencyOnEnclosingOwner()) {

			final Obj owner = enclosingLocal.getOwner();

			walker.up(object, this, owner);

			return owner;
		}

		walker.dep(object, this, this.dependency);

		return this.dependency.getContainer();
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {

		final Dep dep;

		if (dependencyOnEnclosingOwner()) {
			dep = new Dep(scope.getContainer().toObject());
		} else {
			dep = new Dep(
				scope.getContainer().toObject(),
				getDependency().getKey());
		}

		return reproducedPath(dep.toPath());
	}

	@Override
	public HostOp write(CodeDirs dirs, HostOp start) {

		final ObjectOp object = start.toObject(dirs);

		assert object != null :
			"Not an object: " + start;

		return object.dep(dirs, this);
	}

	@Override
	public String toString() {
		if (this.dependency != null) {
			return "Dep[" + this.dependency + " of " + this.object + ']';
		}
		return "Dep[<owner> of " + this.object + ']';
	}

}

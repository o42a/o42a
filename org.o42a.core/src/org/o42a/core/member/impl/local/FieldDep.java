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
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;
import org.o42a.core.member.local.DepKind;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;


public final class FieldDep extends Dep {

	private final Field<?> depField;

	public FieldDep(Obj object, MemberKey dependencyKey) {
		super(object, DepKind.FIELD_DEP);

		final Container container =
				object.getScope().getEnclosingContainer();

		assert container.toLocal() != null :
			object + " is not a local object";

		this.depField = container.member(dependencyKey).toField(dummyUser());

		assert this.depField != null :
			"Dependency " + dependencyKey + " of " + object + " not found";
	}

	@Override
	public Object getKey() {
		return this.depField.getKey();
	}

	@Override
	public String getName() {
		return this.depField.getKey().getName();
	}

	@Override
	public final Field<?> getDepField() {
		return this.depField;
	}

	@Override
	public final Ref getDepRef() {
		return null;
	}

	@Override
	public final Artifact<?> getTarget() {
		return this.depField.getArtifact();
	}

	@Override
	public PathFragment materialize() {
		if (getDepField().getArtifactKind().isObject()) {
			return null;
		}
		return MATERIALIZE;
	}

	@Override
	public PathReproduction reproduce(
			LocationInfo location,
			Reproducer reproducer,
			Scope scope) {
		return reproducedPath(
				new FieldDep(scope.toObject(), getDepField().getKey())
				.toPath());
	}

	@Override
	public int hashCode() {
		return this.depField.getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final FieldDep other = (FieldDep) obj;

		return this.depField.getKey().equals(other.depField.getKey());
	}

	@Override
	public String toString() {
		if (this.depField == null) {
			return super.toString();
		}
		return "Dep[" + this.depField + " of " + getObject() + ']';
	}

	@Override
	protected Container resolveDep(
			PathResolver resolver,
			Path path,
			int index,
			Obj object,
			LocalScope enclosingLocal,
			PathWalker walker) {

		final Member member = enclosingLocal.member(this.depField.getKey());

		walker.fieldDep(object, this, this.depField);

		return member.substance(resolver);
	}

}

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
package org.o42a.core.ref.impl;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;


public class AncestorRef extends Wrap {

	private final Ref ref;

	public AncestorRef(LocationInfo location, Ref ref) {
		super(location, ref.distribute());
		this.ref = ref;
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref + "^^";
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution resolution = this.ref.getResolution();

		if (resolution.isError()) {
			return errorRef(this);
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			getLogger().notArtifact(resolution);
			return errorRef(this);
		}

		final Path path = this.ref.getPath();

		if (path != null) {

			final Path upPath = path.cutArtifact();

			if (upPath != path) {

				assert artifact != null :
					this + " is not resolved to artifact";

				final TypeRef ancestor;
				final TypeRef typeRef = artifact.getTypeRef();

				if (typeRef != null) {
					ancestor = typeRef;
				} else {
					ancestor = artifact.materialize().type().getAncestor();
				}

				final TypeRef rescopedAncestor =
						ancestor.rescope(
								upPath.bind(this, getScope()).rescoper());

				return rescopedAncestor.getRescopedRef();
			}
		}

		final Obj object = artifact.toObject();

		if (object == null) {
			throw new UnsupportedOperationException(
					"Can not build ancestor of " + this.ref);
		}

		final TypeRef ancestor = object.type().getAncestor();
		final Rescoper rescoper;
		final Path objectEnclosingPath =
				object.getScope().getEnclosingScopePath();

		if (path != null) {
			rescoper =
					path.append(objectEnclosingPath)
					.bind(this, getScope())
					.rescoper();
		} else {

			final Ref ancestorScopeRef =
					objectEnclosingPath.target(this, distribute(), this.ref);

			rescoper = ancestorScopeRef.toRescoper();
		}

		return ancestor.rescope(rescoper).getRef();
	}

}

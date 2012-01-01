/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.Path.materializePath;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.ValueType;


public class AncestorFragment extends PathFragment {

	@Override
	public BoundPath expand(PathExpander expander, int index, Scope start) {

		final Artifact<?> artifact = start.getArtifact();

		assert artifact != null :
			"Only artifact may have an ancestor: " + start;

		final Obj object = artifact.toObject();

		if (object == null) {

			final TypeRef typeRef = artifact.getTypeRef();

			return materializePath()
					.bind(typeRef, start)
					.append(ancestor(
						artifact.materialize().getScope(),
						typeRef));
		}

		final Artifact<?> materializationOf = object.getMaterializationOf();

		if (materializationOf != object) {
			return ancestor(start, materializationOf.getTypeRef());
		}

		return ancestor(start, object.type().getAncestor());
	}

	@Override
	public String toString() {
		return "^^";
	}

	private BoundPath ancestor(Scope objectScope, TypeRef ancestor) {
		if (ancestor == null) {
			return ValueType.VOID
					.path(objectScope.getContext().getIntrinsics())
					.bind(ancestor, objectScope);
		}
		return objectScope.getEnclosingScopePath()
				.bind(ancestor, objectScope)
				.append(ancestor.getPath());
	}

}

/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;


public class ClauseObjectRef extends Wrap {

	public ClauseObjectRef(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public String toString() {
		return "$object$";
	}

	@Override
	protected Ref resolveWrapped() {

		final Path path = path();

		if (path == null) {
			return errorRef(this);
		}

		return path.target(this, distribute());
	}

	private Path path() {

		Scope scope = getScope();
		Path path = Path.SELF_PATH;

		for (;;) {

			final Clause clause = scope.getContainer().toClause();

			if (clause == null) {

				final Obj object = scope.toObject();

				if (object == null) {
					getLogger().error(
							"unresolved_object_intrinsic",
							this,
							"Enclosing object not found");
					return null;
				}

				return path;
			}

			final Scope enclosingScope = scope.getEnclosingScope();

			if (enclosingScope == null) {
				return null;
			}

			path = path.append(scope.getEnclosingScopePath());
			scope = enclosingScope;
		}
	}

}

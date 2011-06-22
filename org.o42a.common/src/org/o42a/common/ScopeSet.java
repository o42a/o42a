/*
    Modules Commons
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
package org.o42a.common;

import static org.o42a.core.artifact.object.Derivation.IMPLICIT_SAMPLE;
import static org.o42a.util.use.User.dummyUser;

import java.util.HashSet;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectType;


public class ScopeSet {

	private HashSet<Scope> scopes;

	public boolean add(Scope scope) {
		if (this.scopes == null) {
			this.scopes = new HashSet<Scope>(1);
			this.scopes.add(scope);
			return true;
		}

		if (checkContains(scope)) {
			return false;
		}

		this.scopes.add(scope);

		return true;
	}

	public boolean contains(Scope scope) {
		if (this.scopes == null) {
			return false;
		}
		return checkContains(scope);
	}

	private boolean checkContains(Scope scope) {
		if (this.scopes.contains(scope)) {
			return true;
		}

		final Obj object = scope.toObject();

		if (object == null) {
			return false;
		}

		final ObjectType scopeType = object.type(dummyUser());

		for (Scope s : this.scopes) {

			final Obj obj = s.getContainer().toObject();

			if (obj == null) {
				continue;
			}

			final ObjectType type = obj.type(dummyUser());

			if (scopeType.derivedFrom(type, IMPLICIT_SAMPLE)
					|| type.derivedFrom(scopeType, IMPLICIT_SAMPLE)) {
				this.scopes.add(scope);
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		if (this.scopes == null) {
			return "[]";
		}
		return this.scopes.toString();
	}

}

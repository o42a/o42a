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
package org.o42a.core.artifact;

import static org.o42a.core.ref.common.RoleResolver.expectedRoleOf;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


/**
 * The role some artifact can play for another one.
 *
 * <p>I.e. Can it be a prototype, accessible as instance or not accessible
 * at all.</p>
 *
 * <p>Enumeration values are ordered from a weakest role ({@link #NONE})
 * to a strongest one ({@link #INSTANCE}).</p>
 */
public enum Role {

	/**
	 * Artifact is not accessible at all.
	 *
	 * <p>This can happen e.g. when accessing a member of prototype.</p>
	 */
	NONE() {

		@Override
		public void reportMisuseBy(LocationInfo user, ScopeInfo target) {
		}

	},

	/**
	 * Artifact is accessible.
	 *
	 * <p>This is a wildcard and is used only when checking the role.</p>
	 */
	ANY() {

		@Override
		public void reportMisuseBy(LocationInfo user, ScopeInfo target) {
			target.getContext().getLogger().forbiddenAccess(
					user,
					target);
		}

	},

	/**
	 * Artifact can be used as a prototype. An instance should be constructed
	 * prior to it's use.
	 */
	PROTOTYPE() {

		@Override
		public void reportMisuseBy(LocationInfo user, ScopeInfo target) {
			target.getContext().getLogger().cantInherit(user, target);
		}

	},

	/**
	 * Artifact instance is accessible.
	 */
	INSTANCE() {

		@Override
		public void reportMisuseBy(LocationInfo user, ScopeInfo target) {
			target.getContext().getLogger().notObject(user, target);
		}

	};

	public final boolean atLeast(Role role) {
		return ordinal() >= role.ordinal();
	}

	public abstract void reportMisuseBy(LocationInfo user, ScopeInfo target);

	public final boolean checkUseBy(
			LocationInfo user,
			Ref ref,
			Scope scope) {

		final Role role = expectedRoleOf(ref, scope, this);

		if (role.atLeast(this)) {
			return true;
		}

		reportMisuseBy(user, ref);

		return false;
	}

	public final boolean checkUseBy(ScopeInfo user, Ref ref) {
		return checkUseBy(user, ref, user.getScope());
	}

}

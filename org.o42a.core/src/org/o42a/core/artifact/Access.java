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

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;


public abstract class Access {

	private static final Access ACCESS_BY_OWNER =
		new AccessBy(Accessor.OWNER);
	private static final Access ACCESS_ENCLOSING =
		new AccessBy(Accessor.ENCLOSED);
	private static final Access ACCESS_ENCLOSING_DECLARATION =
		new AccessBy(Accessor.DECLARATION);

	public static Access accessByOwner() {
		return ACCESS_BY_OWNER;
	}

	public static Access accessEnclosing() {
		return ACCESS_ENCLOSING;
	}

	public static Access accessEnclosingDeclaration() {
		return ACCESS_ENCLOSING_DECLARATION;
	}

	public static Access noAccess(ScopeInfo from, ScopeInfo to) {
		return new NoAccess(from, to);
	}

	static Access artifactAccess(PlaceInfo from, Artifact<?> to) {
		if (from == to) {
			return accessByOwner();
		}

		final LocalPlace toPlace = to.getLocalPlace();

		if (toPlace != null) {
			// target is inside the local scope

			final Access result = localArtifactAccess(from, to, toPlace);

			if (result != null) {
				return result;
			}
		}

		return new ArtifactAccess(from, to);
	}

	private static Access localArtifactAccess(
			PlaceInfo from,
			Artifact<?> to,
			LocalPlace toPlace) {

		final LocalPlace fromPlace =
				toPlace.getAppearedIn().placeOf(from);

		if (fromPlace == null) {
			// viewer is not inside the same local scope
			return noAccess(from, to);
		}
		if (fromPlace.equals(toPlace)) {
			// viewer is inside target
			if (to.getContext().declarationsVisibleFrom(
					from.getContext())) {
				// viewer and target are in the same file
				return accessEnclosingDeclaration();
			}
			return accessEnclosing();
		}
		if (!toPlace.getPlace().visibleBy(fromPlace.getPlace())) {
			// viewer appears before target
			return noAccess(from, to);
		}

		return null;
	}

	public final boolean isAccessible() {
		return getRole() != Role.NONE;
	}

	public abstract Role getRole();

	public abstract boolean checkRole(Role use);

	public final boolean checkInstanceUse() {
		return checkRole(Role.INSTANCE);
	}

	public final boolean checkPrototypeUse() {
		return checkRole(Role.PROTOTYPE);
	}

	private static Role role(Artifact<?> to, ScopeInfo user) {

		final Scope userScope = user.getScope();

		if (to.getScope().contains(userScope)) {
			return Role.INSTANCE;
		}

		final Obj enclosingPrototype = to.getEnclosingPrototype();

		if (enclosingPrototype == null
				|| enclosingPrototype.getScope().contains(userScope)) {
			return to.isPrototype() ? Role.PROTOTYPE : Role.INSTANCE;
		}

		return Role.NONE;
	}

	private static final class NoAccess extends Access {

		private final ScopeInfo from;
		private final ScopeInfo to;

		NoAccess(ScopeInfo from, ScopeInfo to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public Role getRole() {
			return Role.NONE;
		}

		@Override
		public boolean checkRole(Role use) {
			if (use == Role.NONE) {
				return true;
			}
			use.reportMisuse(this.from, this.to);
			return false;
		}

	}

	private static final class AccessBy extends Access {

		private final Accessor accessor;

		AccessBy(Accessor accessor) {
			this.accessor = accessor;
		}

		@Override
		public Role getRole() {
			return Role.INSTANCE;
		}

		@Override
		public boolean checkRole(Role use) {
			return true;
		}

		@Override
		public String toString() {
			return "Access by " + this.accessor;
		}

	}

	private static final class ArtifactAccess extends Access {

		private final ScopeInfo from;
		private final Artifact<?> to;
		private Role role;

		ArtifactAccess(ScopeInfo from, Artifact<?> to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public Role getRole() {
			if (this.role == null) {
				this.role = role(this.to, this.from);
			}
			return this.role;
		}

		@Override
		public boolean checkRole(Role expectedRole) {
			if (expectedRole == Role.NONE) {
				return true;
			}
			if (!this.to.isValid()) {
				Role.ANY.reportMisuse(this.to, this.from);
				return false;
			}

			final Role actualRole = getRole();

			if (actualRole.ordinal() < expectedRole.ordinal()) {
				expectedRole.reportMisuse(this.to, this.from);
				return false;
			}

			return true;
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append("Access from ").append(this.from);
			out.append(" to ").append(this.to);
			if (this.role != null) {
				out.append('(').append(this.role).append(')');
			} else {
				out.append("(?)");
			}

			return out.toString();
		}

	}

}

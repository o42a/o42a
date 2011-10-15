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
package org.o42a.core.ref.common;

import static org.o42a.core.artifact.Role.*;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.Role;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class RoleResolver implements ResolutionWalker, PathWalker {

	public static Role expectedRoleOf(Ref ref, Scope scope, Role expectedRole) {
		ref.assertCompatible(scope);

		final RoleResolver roleResolver = new RoleResolver(expectedRole);
		final Resolver resolver =
				scope.walkingResolver(dummyUser(), roleResolver);
		final Resolution resolution = ref.resolve(resolver);

		if (resolution == null) {
			return NONE;
		}

		return roleResolver.getRole();
	}

	public static final Role expectedRoleOf(Ref ref, Role expectedRole) {
		return expectedRoleOf(ref, ref.getScope(), expectedRole);
	}

	public static final Role roleOf(Ref ref, Scope scope) {
		return expectedRoleOf(ref, scope, Role.ANY);
	}

	public static final Role roleOf(Ref ref) {
		return expectedRoleOf(ref, ref.getScope(), Role.ANY);
	}

	private final Role expectedRole;
	private Role role = INSTANCE;
	private boolean insidePrototype;

	public RoleResolver() {
		this.expectedRole = ANY;
	}

	public RoleResolver(Role expectedRole) {
		this.expectedRole = expectedRole != null ? expectedRole : ANY;
	}

	public final Role getExpectedRole() {
		return this.expectedRole;
	}

	public final Role getRole() {
		if (this.insidePrototype) {
			this.insidePrototype = false;
			updateRole(PROTOTYPE);
		}
		return this.role;
	}

	@Override
	public PathWalker path(LocationInfo location, BoundPath path) {
		return this;
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {
		return mayProceedInsidePrototype();
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		return mayProceedInsidePrototype();
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		// It is ok to enter prototype member and exit it right after that.
		// This way expression phrases work.
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		if (this.insidePrototype || !this.role.atLeast(INSTANCE)) {
			this.insidePrototype = false;
			// An attempt to access the member of prototype.
			return updateRole(Role.NONE);
		}

		final Field<?> field = member.toField(dummyUser());

		if (field != null && field.isPrototype()) {
			this.insidePrototype = true;
		}

		return true;
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		if (!mayProceedInsidePrototype()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean fieldDep(Obj object, Step step, Field<?> dependency) {
		if (!mayProceedInsidePrototype()) {
			return false;
		}
		if (dependency.isPrototype()) {
			this.insidePrototype = true;
		}
		return true;
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		if (!mayProceedInsidePrototype()) {
			return false;
		}

		final LocalScope local =
				object.getScope().getEnclosingScope().toLocal();
		final LocalResolver resolver = local.walkingResolver(dummyUser(), this);
		final Resolution resolution = dependency.resolve(resolver);

		return resolution != null;
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		return true;
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		this.insidePrototype = false;
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	protected boolean updateRole(Role role) {
		this.role = role;
		return role.atLeast(getExpectedRole());
	}

	private boolean mayProceedInsidePrototype() {
		if (!this.insidePrototype) {
			return true;
		}
		this.insidePrototype = false;
		return updateRole(NONE);
	}

}

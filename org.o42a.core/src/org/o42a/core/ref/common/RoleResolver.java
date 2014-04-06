/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.core.object.Role.*;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.Role;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class RoleResolver implements PathWalker {

	public static Role expectedRoleOf(Ref ref, Scope scope, Role expectedRole) {
		return expectedRoleOf(ref, ref, scope, expectedRole);
	}

	public static Role expectedRoleOf(
			LocationInfo user,
			Ref ref,
			Scope scope,
			Role expectedRole) {
		ref.assertCompatible(scope);

		final RoleResolver roleResolver =
				new RoleResolver(user, expectedRole);
		final Resolver resolver = scope.walkingResolver(roleResolver);
		final Resolution resolution = ref.resolve(resolver);

		if (!resolution.isResolved()) {
			return NONE;
		}

		return roleResolver.getRole();
	}

	public static final Role expectedRoleOf(Ref ref, Role expectedRole) {
		return expectedRoleOf(ref, ref.getScope(), expectedRole);
	}

	public static final Role expectedRoleOf(
			LocationInfo user,
			Ref ref,
			Role expectedRole) {
		return expectedRoleOf(user, ref, ref.getScope(), expectedRole);
	}

	public static final Role roleOf(Ref ref, Scope scope) {
		return expectedRoleOf(ref, scope, Role.ANY);
	}

	public static final Role roleOf(Ref ref) {
		return expectedRoleOf(ref, ref.getScope(), Role.ANY);
	}

	private final LocationInfo user;
	private final Role expectedRole;
	private Role role;
	private boolean insidePrototype;

	public RoleResolver(LocationInfo user) {
		this.user = user;
		this.expectedRole = ANY;
	}

	public RoleResolver(LocationInfo user, Role expectedRole) {
		this.user = user;
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
	public boolean root(BoundPath path, Scope root) {
		this.insidePrototype = false;
		this.role = Role.INSTANCE;
		return true;
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		this.insidePrototype = false;
		this.role = Role.INSTANCE;
		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		this.insidePrototype = false;
		return true;
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
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
			if (updateRole(Role.NONE)) {
				return true;
			}
			getExpectedRole().reportMisuseBy(this.user, member);
			return false;
		}

		final MemberField field = member.toField();

		if (field != null && field.isPrototype()) {
			this.insidePrototype = true;
		}

		return true;
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		if (mayProceedInsidePrototype()) {
			return true;
		}
		getExpectedRole().reportMisuseBy(this.user, link);
		return false;
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return local.ref().resolve(scope.walkingResolver(this)).isResolved();
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		if (!mayProceedInsidePrototype()) {
			getExpectedRole().reportMisuseBy(dep.ref(), object);
			return false;
		}

		final Scope enclosingScope = dep.enclosingScope(object.getScope());

		return dep.ref()
				.resolve(enclosingScope.walkingResolver(this))
				.isResolved();
	}

	@Override
	public boolean object(Step step, Obj object) {
		if (mayProceedInsidePrototype()) {
			return true;
		}

		final ObjectConstructor constructor = step.getConstructor();

		if (constructor != null && constructor.isAllowedInsidePrototype()) {
			this.insidePrototype = false;
			return true;
		}

		getExpectedRole().reportMisuseBy(this.user, object);

		return false;
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		this.insidePrototype = false;
	}

	@Override
	public boolean done(Container result) {
		if (getRole().atLeast(getExpectedRole())) {
			return true;
		}
		getExpectedRole().reportMisuseBy(this.user, result);
		return false;
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

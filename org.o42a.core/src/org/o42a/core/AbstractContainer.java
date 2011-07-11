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
package org.o42a.core;

import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public abstract class AbstractContainer extends Location implements Container {

	public static Path findContainerPath(
			Container container,
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {

		final Path result = container.findMember(user, memberId, declaredIn);

		if (result != null) {
			return result;
		}

		final Container enclosing = container.getEnclosingContainer();

		if (enclosing == null) {
			return null;
		}

		final Path found = enclosing.findPath(user, memberId, declaredIn);

		if (found == null) {
			return null;
		}
		if (found.isAbsolute()) {
			return found;
		}
		if (enclosing.getScope() == container.getScope()) {
			return found;
		}

		final Container resolved =
			found.resolve(enclosing, dummyUser(), enclosing.getScope());

		if (resolved.getScope() == container.getScope()) {
			return SELF_PATH;
		}

		final Path enclosingScopePath =
			container.getScope().getEnclosingScopePath();

		assert enclosingScopePath != null :
			found + " should be an absolute path";

		return enclosingScopePath.append(found);
	}

	public static Container parentContainer(Container container) {

		final Scope scope = container.getScope();
		final Member member = container.toMember();

		if (member == null || member.getScope() != scope) {
			return scope.getEnclosingContainer();
		}

		final MemberKey enclosingKey = member.getKey().getEnclosingKey();

		if (enclosingKey == null) {
			return scope.getContainer();
		}

		final Member parent = scope.getContainer().member(enclosingKey);

		assert parent != null :
			"Parent container of " + container
			+ " does not exist: " + enclosingKey;

		return parent.getContainer();
	}

	public AbstractContainer(CompilerContext context, LogInfo location) {
		super(context, location);
	}

	public AbstractContainer(LocationInfo location) {
		super(location);
	}

	@Override
	public Container getParentContainer() {
		return parentContainer(this);
	}

	@Override
	public Path findPath(ScopeInfo user, MemberId memberId, Obj declaredIn) {
		return findContainerPath(this, user, memberId, declaredIn);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

}

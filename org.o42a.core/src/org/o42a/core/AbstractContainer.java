/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Located;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.log.LogInfo;


public abstract class AbstractContainer extends Located implements Container {

	public static Container parentContainer(Container container) {

		final Scope scope = container.getScope();
		final Member member = container.toMember();

		if (member == null || member.getScope() != scope) {
			return scope.getEnclosingContainer();
		}

		final MemberKey enclosingKey = member.getMemberKey().getEnclosingKey();

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

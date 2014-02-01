/*
    Compiler
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.access;

import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.CheckResult;


public class ParentAccessRules extends ProxyAccessRules {

	private final Container parent;

	public ParentAccessRules(Container parent, AccessRules parentRules) {
		super(parentRules);
		this.parent = parent;
	}

	@Override
	public Ref selfRef(
			Interpreter ip,
			LocationInfo location,
			AccessDistributor distributor) {
		return parentRef(location, distributor, null);
	}

	@Override
	public CheckResult checkContainerAccessibility(
			LocationInfo location,
			Container from,
			Container to) {
		if (isInsideParent(to)) {
			return CheckResult.CHECK_SKIP;
		}
		return getWrapped().checkContainerAccessibility(location, from, to);
	}

	@Override
	public boolean containerIsVisible(Container by, Container what) {
		if (isInsideParent(what)) {
			return false;
		}
		return getWrapped().containerIsVisible(by, what);
	}

	@Override
	protected AccessRules wrap(AccessRules wrapped) {
		return new ParentAccessRules(this.parent, wrapped);
	}

	private boolean isInsideParent(Container container) {

		final Scope parentScope = this.parent.getScope();
		final Scope scope = container.getScope();

		return !parentScope.is(scope) && parentScope.contains(scope);
	}

}

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
package org.o42a.core.ref.impl.normalizer;

import static java.util.Collections.singletonList;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


public class PropagatedMultiScope extends MultiScope {

	public PropagatedMultiScope(Scope scope) {
		super(scope);
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return MultiScopeSet.DERIVED_SCOPES;
	}

	@Override
	public Iterator<Scope> iterator() {
		return singletonList(getScope()).iterator();
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope.toString() + '*';
	}

}

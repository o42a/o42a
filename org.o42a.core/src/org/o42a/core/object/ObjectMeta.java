/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.util.fn.Init.init;

import org.o42a.core.Scope;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.meta.ObjectMetaBase;
import org.o42a.util.fn.Init;


public final class ObjectMeta extends ObjectMetaBase {

	private final Init<Nesting> nesting =
			init(() -> getObject().createNesting());

	ObjectMeta(Obj object) {
		super(object);
	}

	@Override
	public boolean isUpdated() {
		getObject().resolveMembers(true);
		getObject().type().resolve(false);
		return super.isUpdated();
	}

	public final Nesting getNesting() {
		return this.nesting.get();
	}

	public final Obj findIn(Scope enclosing) {

		final Scope enclosingScope = getObject().getScope().getEnclosingScope();

		if (enclosingScope.is(enclosing)) {
			return getObject();
		}

		enclosing.assertDerivedFrom(enclosingScope);

		return getNesting().findObjectIn(enclosing);
	}

}

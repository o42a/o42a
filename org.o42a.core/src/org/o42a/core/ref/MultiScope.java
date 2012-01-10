/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref;

import static org.o42a.core.ref.impl.normalizer.DerivativesMultiScope.objectMultiScope;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.impl.normalizer.PropagatedMultiScope;


public abstract class MultiScope implements Iterable<Scope> {

	public static MultiScope multiScope(Scope start) {

		final Obj object = start.toObject();

		if (object != null) {
			return objectMultiScope(object);
		}

		final LocalScope local = start.toLocal();

		assert local != null :
			"Can not build multi-scope for " + start;

		return new PropagatedMultiScope(local);
	}

	private final Scope scope;

	public MultiScope(Scope scope) {
		this.scope = scope;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public abstract MultiScopeSet getScopeSet();

	public Iterable<Scope> ancestors() {
		return null;
	}

}

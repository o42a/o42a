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
package org.o42a.core.ref.impl.normalizer;

import static java.util.Collections.singletonList;
import static org.o42a.util.use.User.dummyUser;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.type.TypeRef;


final class InheritedMultiScope extends MultiScope {

	private final MultiScope ancestors;

	static MultiScope inheritedMultiScope(Obj object) {

		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor.isStatic()) {
			return new PropagatedMultiScope(object.getScope());
		}

		final MultiWalker ancestorWalker = new MultiWalker();
		final Resolution ancestorResolution =
				ancestor.resolve(ancestor.getScope().walkingResolver(
						dummyUser(),
						ancestorWalker));

		if (!ancestorResolution.isResolved()) {
			return null;
		}

		final MultiScope ancestors = ancestorWalker.getMultiScope();

		if (ancestors.getScopeSet().nothingButDerived()) {
			return new PropagatedMultiScope(object.getScope());
		}

		return new InheritedMultiScope(object.getScope(), ancestors);
	}

	private InheritedMultiScope(Scope scope, MultiScope ancestors) {
		super(scope);
		this.ancestors = ancestors;
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return MultiScopeSet.INHERITED_SCOPES;
	}

	@Override
	public MultiScope ancestors() {
		return this.ancestors;
	}

	@Override
	public Iterator<Scope> iterator() {
		return singletonList(getScope()).iterator();
	}

	@Override
	public String toString() {
		if (this.ancestors == null) {
			return super.toString();
		}
		return ("InheriterMultiScope[" + getScope()
				+ " from " + this.ancestors + ']');
	}

}

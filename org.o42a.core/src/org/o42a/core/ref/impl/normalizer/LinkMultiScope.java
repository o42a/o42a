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
import static org.o42a.core.ref.impl.normalizer.DerivativesMultiScope.objectMultiScope;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


final class LinkMultiScope extends MultiScope {

	static MultiScope declaredFieldMultiScope(Field<?> field) {

		final Obj object = field.toObject();

		if (object != null) {
			return objectMultiScope(object);
		}

		return new LinkMultiScope(field);
	}

	private LinkMultiScope(Field<?> scope) {
		super(scope);
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return MultiScopeSet.SCOPES;
	}

	@Override
	public Iterator<Scope> iterator() {
		return singletonList(getScope()).iterator();
	}

	@Override
	public MultiScope materialize() {
		return objectMultiScope(
				getScope().toField().getArtifact().materialize());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "LinkMultiScope[" + getScope() + ']';
	}

}

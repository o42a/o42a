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

import static org.o42a.core.ref.impl.normalizer.InheritedMultiScope.inheritedMultiScope;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Derivative;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


public final class DerivativesMultiScope extends MultiScope {

	public static MultiScope objectMultiScope(Obj object) {
		if (object.getConstructionMode().isStrict()) {
			return inheritedMultiScope(object);
		}
		if (object.type().allDerivatives().isEmpty()) {
			return new PropagatedMultiScope(object.getScope());
		}
		return new DerivativesMultiScope(object);
	}

	private DerivativesMultiScope(Obj object) {
		super(object.getScope());
	}

	@Override
	public final MultiScopeSet getScopeSet() {
		return MultiScopeSet.DERIVED_SCOPES;
	}

	@Override
	public Iterator<Scope> iterator() {
		return new DerivativesIterator(getScope().toObject());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "DerivativesMultiScope[" + scope.toObject() + ']';
	}

	private static final class DerivativesIterator implements Iterator<Scope> {

		private final Obj start;
		private Iterator<Derivative> derivatives;
		private DerivativesIterator sub;

		DerivativesIterator(Obj start) {
			this.start = start;
		}

		@Override
		public boolean hasNext() {
			if (this.derivatives == null) {
				return true;
			}
			if (this.sub != null && this.sub.hasNext()) {
				return true;
			}
			return this.derivatives.hasNext();
		}

		@Override
		public Scope next() {
			if (this.derivatives == null) {
				this.derivatives =
						this.start.type().allDerivatives().iterator();
				return this.start.getScope();
			}
			if (this.sub == null || !this.sub.hasNext()) {
				this.sub = new DerivativesIterator(
						this.derivatives.next().getDerivedObject());
			}
			return this.sub.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			if (this.start == null) {
				return super.toString();
			}
			return "DerivativesIterator[" + this.start + ']';
		}

	}

}

/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.prediction;

import static java.util.Collections.singletonList;
import static org.o42a.core.ref.impl.prediction.PredictionWalker.predictRef;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.TypeRef;


final class ObjectImplementations extends Prediction {

	private final Prediction enclosing;
	private Prediction ancestorPrediction;

	ObjectImplementations(Prediction enclosing, Obj object) {
		super(object.getScope());
		this.enclosing = enclosing;
		object.getScope().getEnclosingScope().assertDerivedFrom(
				enclosing.getScope());
	}

	@Override
	public Predicted getPredicted() {
		return getAncestorPrediction().getPredicted();
	}

	@Override
	public Iterator<Scope> iterator() {

		final Prediction ancestorPrediction = getAncestorPrediction();

		if (!ancestorPrediction.isPredicted()) {
			return Collections.<Scope>emptyList().iterator();
		}
		if (ancestorPrediction.isExact()) {
			return singletonList(getScope()).iterator();
		}

		return new Iter(getScope().toObject(), ancestorPrediction);
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "ObjectImplementations[" + scope + ']';
	}

	private Prediction getAncestorPrediction() {
		if (this.ancestorPrediction != null) {
			return this.ancestorPrediction;
		}

		final TypeRef ancestor = getScope().toObject().type().getAncestor();

		if (ancestor == null || ancestor.isStatic()) {
			return this.ancestorPrediction = exactPrediction(getScope());
		}

		return this.ancestorPrediction =
				predictRef(ancestor.getRef(), this.enclosing);
	}

	private static final class Iter implements Iterator<Scope> {

		private final Obj object;
		private final BoundPath ancestorPath;
		private final Iterator<Scope> ancestors;
		private Obj next;

		Iter(Obj object, Prediction ancestorPrediction) {
			this.object = object;
			this.ancestorPath = object.type().getAncestor().getPath();
			this.ancestors = ancestorPrediction.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.next != null || findNext();
		}

		@Override
		public Scope next() {
			if (this.next == null) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}

			final Obj next = this.next;

			this.next = null;

			return next.getScope();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private boolean findNext() {

			final Scope objectEnclosing =
					this.object.getScope().getEnclosingScope();

			while (this.ancestors.hasNext()) {

				final Scope ancestor = this.ancestors.next();
				final Scope enclosing = this.ancestorPath.revert(ancestor);

				if (!enclosing.derivedFrom(objectEnclosing)) {
					objectEnclosing.assertDerivedFrom(enclosing);
					continue;
				}

				this.next = this.object.findIn(enclosing);

				return true;
			}

			return false;
		}
	}

}

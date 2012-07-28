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

import static org.o42a.core.ref.impl.prediction.PredictionWalker.predictRef;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;
import org.o42a.core.ref.type.TypeRef;


final class ObjectImplementations extends Prediction {

	private final Prediction basePrediction;
	private final Pred object;
	private Prediction ancestorPrediction;

	ObjectImplementations(Prediction basePrediction, Pred object) {
		super(object.getScope());
		this.basePrediction = basePrediction;
		this.object = object;
		object.getScope().getEnclosingScope().assertDerivedFrom(
				basePrediction.getScope());
	}

	@Override
	public Predicted getPredicted() {
		return getAncestorPrediction().getPredicted();
	}

	@Override
	public Iterator<Pred> iterator() {

		final Prediction ancestorPrediction = getAncestorPrediction();

		if (!ancestorPrediction.isPredicted()) {
			return Collections.<Pred>emptyList().iterator();
		}
		if (ancestorPrediction.isExact()) {
			return this.object.iterator();
		}

		return new Itr(this.object, ancestorPrediction);
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
			return this.ancestorPrediction =
					exactPrediction(this.basePrediction, getScope());
		}

		return this.ancestorPrediction =
				predictRef(this.basePrediction, ancestor.getRef());
	}

	private static final class Itr implements Iterator<Pred> {

		private final Pred object;
		private final Iterator<Pred> ancestors;
		private Pred next;
		private boolean objectReported;

		Itr(Pred object, Prediction ancestorPrediction) {
			this.object = object;
			this.ancestors = ancestorPrediction.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.next != null || findNext();
		}

		@Override
		public Pred next() {
			if (this.next == null) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}

			final Pred next = this.next;

			this.next = null;

			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private final Obj object() {
			return this.object.getScope().toObject();
		}

		private boolean findNext() {

			final Scope objectEnclosing =
					this.object.getScope().getEnclosingScope();

			while (this.ancestors.hasNext()) {

				final Pred ancestor = this.ancestors.next();

				if (!ancestor.isPredicted()) {
					this.next = ancestor;
					return true;
				}

				final Scope base = ancestor.revert();

				if (!base.derivedFrom(objectEnclosing)) {
					// The inherited ancestor may resolve to incompatible scope.
					// Report the object itself, if not reported already.
					if (this.objectReported) {
						continue;
					}
					this.next = this.object;
					this.objectReported = true;
					return true;
				}

				final Obj object = object();
				final Obj nextObject = object.meta().findIn(base);

				this.next = this.object.setScope(nextObject.getScope());
				this.objectReported |= nextObject.is(object);

				return true;
			}

			return false;
		}

	}

}

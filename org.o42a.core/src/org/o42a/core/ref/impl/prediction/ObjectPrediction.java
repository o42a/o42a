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
package org.o42a.core.ref.impl.prediction;

import static org.o42a.core.ref.impl.prediction.ObjectImplementations.objectImplementations;
import static org.o42a.util.collect.Iterators.singletonIterator;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Derivative;
import org.o42a.core.ref.*;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.collect.SubIterator;


public class ObjectPrediction extends Prediction {

	public static Prediction predictObject(
			Prediction enclosing,
			Obj object) {
		assert enclosing.assertEncloses(object.getScope());

		switch (enclosing.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(enclosing, object.getScope());
		case UNPREDICTED:
			return unpredicted(object.getScope());
		case PREDICTED:
			return new ObjectPrediction(enclosing, object);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + enclosing.getPredicted());
	}

	private final Prediction enclosing;

	private ObjectPrediction(Prediction enclosing, Obj object) {
		super(object.getScope());
		this.enclosing = enclosing;
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public ReadonlyIterator<Pred> iterator() {
		return new Itr(this.enclosing, getScope().toObject());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "ObjectPrediction[" + scope + ']';
	}

	private static final class Itr extends SubIterator<Pred, Pred> {

		private final Obj object;

		Itr(Prediction basePrediction, Obj object) {
			super(basePrediction.iterator());
			this.object = object;
		}

		@Override
		protected Iterator<Pred> nestedIterator(Pred nextBase) {
			if (!nextBase.isPredicted()) {
				return nextBase.iterator();
			}

			final Obj start =
					this.object.meta().findIn(nextBase.getScope());

			return new ObjectPredIterator(
					nextBase,
					singletonIterator(start)
					.then(new DerivativesIterator(start)));
		}

	}

	private static final class DerivativesIterator
			extends SubIterator<Obj, Derivative> {

		private final Obj start;

		DerivativesIterator(Obj start) {
			super(start.type().updatedDerivatives().iterator());
			this.start = start;
		}

		@Override
		public String toString() {
			if (this.start == null) {
				return super.toString();
			}
			return "DerivativesIterator[" + this.start + ']';
		}

		@Override
		protected Iterator<Obj> nestedIterator(Derivative baseElement) {

			final Obj derivedObject = baseElement.getDerivedObject();

			return singletonIterator(derivedObject)
					.then(new DerivativesIterator(derivedObject));
		}

	}

	private static final class ObjectPredIterator
			extends SubIterator<Pred, Obj> {

		private final Pred base;

		ObjectPredIterator(Pred base, ReadonlyIterator<Obj> derivatives) {
			super(derivatives);
			this.base = base;
		}

		@Override
		protected Iterator<Pred> nestedIterator(Obj object) {
			object.getScope().getEnclosingScope().assertDerivedFrom(
					this.base.getScope());

			final ObjectPred pred = new ObjectPred(this.base, object);

			return objectImplementations(pred);
		}

	}

	private static final class ObjectPred extends DerivedPred {

		ObjectPred(Pred base, Obj object) {
			super(base, object.getScope());
		}

		@Override
		protected Scope baseOf(Scope derived) {
			if (!derived.derivedFrom(getScope())) {
				assert getScope().assertDerivedFrom(derived);
				return getScope().getEnclosingScope();
			}
			return derived.getEnclosingScope();
		}

	}

}

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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Derivative;
import org.o42a.core.ref.*;


public class ObjectPrediction extends Prediction {

	public static Prediction predictObject(
			Prediction basePrediction,
			Obj object) {
		assert basePrediction.assertEncloses(object.getScope());

		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(basePrediction, object.getScope());
		case UNPREDICTED:
			return unpredicted(object.getScope());
		case PREDICTED:
			return new ObjectPrediction(basePrediction, object);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	private final Prediction basePrediction;

	private ObjectPrediction(Prediction basePrediction, Obj object) {
		super(object.getScope());
		this.basePrediction = basePrediction;
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Pred> iterator() {
		return new Itr(this.basePrediction, getScope().toObject());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "ObjectPrediction[" + scope + ']';
	}

	private static final class Itr implements Iterator<Pred> {

		private final Prediction basePrediction;
		private final Iterator<Pred> bases;
		private final Obj object;
		private Iterator<Pred> overriders;

		Itr(Prediction basePrediction, Obj object) {
			this.basePrediction = basePrediction;
			this.bases = basePrediction.iterator();
			this.object = object;
		}

		@Override
		public boolean hasNext() {
			if (this.overriders == null || !this.overriders.hasNext()) {
				return findNext();
			}
			return this.overriders.hasNext();
		}

		@Override
		public Pred next() {
			if (this.overriders == null || !this.overriders.hasNext()) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}
			return this.overriders.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private boolean findNext() {
			do {
				if (!this.bases.hasNext()) {
					return false;
				}

				final Pred nextBase = this.bases.next();

				if (!nextBase.isPredicted()) {
					this.overriders = nextBase.iterator();
					return true;
				}

				this.overriders = new ObjItr(
						this.basePrediction,
						nextBase,
						new DerivativesIterator(
								this.object.findIn(nextBase.getScope())));
			} while (this.overriders == null || !this.overriders.hasNext());
			return true;
		}

	}

	private static final class DerivativesIterator implements Iterator<Obj> {

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
		public Obj next() {
			if (this.derivatives == null) {
				this.derivatives =
						this.start.type().allDerivatives().iterator();
				return this.start;
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

	private static final class ObjItr implements Iterator<Pred> {

		private final Prediction basePrediction;
		private final Pred base;
		private final DerivativesIterator derivatives;
		private Iterator<Pred> impls;

		ObjItr(
				Prediction basePrediction,
				Pred base,
				DerivativesIterator derivatives) {
			this.basePrediction = basePrediction;
			this.base = base;
			this.derivatives = derivatives;
		}

		@Override
		public boolean hasNext() {
			if (this.impls == null || !this.impls.hasNext()) {
				return nextImpl();
			}
			return true;
		}

		@Override
		public Pred next() {
			if (this.impls == null || !this.impls.hasNext()) {
				if (!nextImpl()) {
					throw new NoSuchElementException();
				}
			}
			return this.impls.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private boolean nextImpl() {
			do {
				if (!this.derivatives.hasNext()) {
					return false;
				}

				final Obj object = this.derivatives.next();

				object.getScope().getEnclosingScope().assertDerivedFrom(
						this.base.getScope());

				final ObjectImplementations impls =
						new ObjectImplementations(
								this.basePrediction,
								new ObjectPred(this.base, object));

				this.impls = impls.iterator();
			} while (this.impls == null || !this.impls.hasNext());

			return true;
		}

	}

	private static final class ObjectPred extends DerivedPred {

		ObjectPred(Pred base, Obj object) {
			super(base, object.getScope());
		}

		@Override
		protected Scope baseOf(Scope derived) {
			if (!derived.derivedFrom(getScope())) {
				getScope().assertDerivedFrom(derived);
				return getScope().getEnclosingScope();
			}
			return derived.getEnclosingScope();
		}

	}

}

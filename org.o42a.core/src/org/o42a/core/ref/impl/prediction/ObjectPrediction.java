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
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;


public class ObjectPrediction extends Prediction {

	public static Prediction predictObject(
			Prediction enclosing,
			Obj object) {
		assert enclosing.assertEncloses(object.getScope());

		switch (enclosing.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(object.getScope());
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
	public Iterator<Scope> iterator() {
		return new Iter(
				this.enclosing,
				new DerivativesIterator(getScope().toObject()));
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "ObjectPrediction[" + scope + ']';
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

	private static final class Iter implements Iterator<Scope> {

		private final Prediction enclosing;
		private final DerivativesIterator derivatives;
		private Iterator<Scope> impls;

		Iter(Prediction enclosing, DerivativesIterator derivatives) {
			this.enclosing = enclosing;
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
		public Scope next() {
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
				final ObjectImplementations impls =
						new ObjectImplementations(this.enclosing, object);

				this.impls = impls.iterator();
			} while (this.impls == null || !this.impls.hasNext());

			return true;
		}

	}
}

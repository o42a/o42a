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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.util.collect.ReadonlyIterator;


public class EnclosingPrediction extends Prediction {

	public static Prediction enclosingPrediction(
			Prediction basePrediction,
			Scope scope,
			Path enclosingPath,
			ReversePath reversePath) {
		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(basePrediction, scope);
		case UNPREDICTED:
			return unpredicted(scope);
		case PREDICTED:
			return new EnclosingPrediction(
					basePrediction,
					scope,
					enclosingPath,
					reversePath);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	private final Prediction basePrediction;
	private final Path enclosingPath;
	private final ReversePath reversePath;

	private EnclosingPrediction(
			Prediction basePrediction,
			Scope scope,
			Path enclosingPath,
			ReversePath reversePath) {
		super(scope);
		this.basePrediction = basePrediction;
		this.enclosingPath = enclosingPath;
		this.reversePath = reversePath;
	}

	@Override
	public ReadonlyIterator<Pred> iterator() {
		return new Itr(this);
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public String toString() {
		if (this.basePrediction == null) {
			return super.toString();
		}
		return "EnclosingPrediction[" + this.basePrediction + ']';
	}

	private static final class Itr extends ReadonlyIterator<Pred> {

		private final EnclosingPrediction prediction;
		private final Iterator<Pred> bases;

		Itr(EnclosingPrediction prediction) {
			this.prediction = prediction;
			this.bases = prediction.basePrediction.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.bases.hasNext();
		}

		@Override
		public Pred next() {

			final Pred base = this.bases.next();

			if (!base.isPredicted()) {
				return base;
			}

			final BoundPath path = this.prediction.enclosingPath.bind(
					base.getScope(),
					base.getScope());
			final PathResolution resolution = path.resolve(
					pathResolver(path.getOrigin(), dummyUser()));

			return new EnclosingPred(
					base,
					resolution.getResult().getScope(),
					this.prediction.reversePath);
		}

	}

	private static final class EnclosingPred extends DerivedPred {

		private final ReversePath revertPath;

		EnclosingPred(Pred base, Scope scope, ReversePath revertPath) {
			super(base, scope);
			this.revertPath = revertPath;
		}

		@Override
		protected Scope baseOf(Scope derived) {
			if (!derived.derivedFrom(getScope())) {
				// The reverse resolution may result to ascendant scope.
				// Report the scope itself.
				// assert getScope().assertDerivedFrom(derived);
				return this.revertPath.revert(getScope());
			}
			return this.revertPath.revert(derived);
		}

	}

}

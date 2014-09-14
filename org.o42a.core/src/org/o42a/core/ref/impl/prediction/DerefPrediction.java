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

import static org.o42a.core.ref.Pred.noPred;
import static org.o42a.core.ref.impl.prediction.EnclosingPrediction.enclosingPrediction;
import static org.o42a.core.ref.impl.prediction.PredictionWalker.predictRef;
import static org.o42a.util.collect.Iterators.emptyIterator;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;
import org.o42a.core.value.link.Link;
import org.o42a.util.collect.ReadonlyIterator;
import org.o42a.util.collect.SubIterator;


final class DerefPrediction extends Prediction {

	public static Prediction derefPrediction(
			Prediction basePrediction,
			Link link) {

		final Scope targetScope = link.getTarget().getScope();

		assert basePrediction.assertEncloses(targetScope);

		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(basePrediction, targetScope);
		case UNPREDICTED:
			return unpredicted(targetScope);
		case PREDICTED:
			return new DerefPrediction(basePrediction, targetScope, link);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	private final Prediction basePrediction;
	private final Link link;

	private DerefPrediction(
			Prediction enclosing,
			Scope scope,
			Link link) {
		super(scope);
		this.basePrediction = enclosing;
		this.link = link;
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public ReadonlyIterator<Pred> iterator() {
		return new Itr(this);
	}

	@Override
	public String toString() {
		if (this.link == null) {
			return super.toString();
		}
		return "DerefPrediction[" + this.link + ']';
	}

	private static final class Itr extends SubIterator<Pred, Pred> {

		private final DerefPrediction prediction;

		Itr(DerefPrediction prediction) {
			super(prediction.basePrediction.iterator());
			this.prediction = prediction;
		}

		@Override
		protected Iterator<? extends Pred> nestedIterator(final Pred base) {

			final Obj linkObject = base.getScope().toObject();
			final DefTarget target =
					linkObject.value().getDefinitions().target();

			if (!target.exists()) {
				return noPred().iterator();
			}
			if (target.isUnknown()) {
				return emptyIterator();
			}

			return predictRef(
					enclosingPrediction(
							new SinglePrediction(
									this.prediction.basePrediction,
									base),
							base.getScope().getEnclosingScope(),
							base.getScope().getEnclosingScopePath(),
							t -> base.getScope()),
					target.getRef()).iterator();
		}

	}

}

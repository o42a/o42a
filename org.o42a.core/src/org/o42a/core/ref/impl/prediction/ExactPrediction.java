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

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.*;


public final class ExactPrediction extends Prediction {

	private final Prediction basePrediction;

	public ExactPrediction(Prediction basePrediction, Scope scope) {
		super(scope);
		this.basePrediction = basePrediction;
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.EXACTLY_PREDICTED;
	}

	@Override
	public Iterator<Pred> iterator() {

		final Pred base = this.basePrediction.iterator().next();

		if (!base.isPredicted()) {
			return base.iterator();
		}

		return new ExactPred(base, getScope()).iterator();
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope + "!";
	}

	private static final class ExactPred extends DerivedPred {

		ExactPred(Pred base, Scope scope) {
			super(base, scope);
		}

		@Override
		public String toString() {

			final Scope scope = getScope();

			if (scope == null) {
				return super.toString();
			}

			return scope + "!";
		}

		@Override
		protected Scope baseOf(Scope derived) {
			return getScope();
		}

	}

}

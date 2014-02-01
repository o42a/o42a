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
package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.core.ref.impl.prediction.ExactPrediction;
import org.o42a.core.ref.impl.prediction.InitialPrediction;
import org.o42a.core.ref.impl.prediction.Unpredicted;
import org.o42a.util.collect.ReadonlyIterable;


public abstract class Prediction implements ReadonlyIterable<Pred> {

	public static Prediction exactPrediction(
			Prediction basePrediction,
			Scope scope) {
		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
		case PREDICTED:
			return new ExactPrediction(basePrediction, scope);
		case UNPREDICTED:
			return unpredicted(scope);
		}
		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	public static Prediction unpredicted(Scope scope) {
		return new Unpredicted(scope);
	}

	public static Prediction startPrediction(Scope scope) {
		return scope.predict(new InitialPrediction(scope.getEnclosingScope()));
	}

	private final Scope scope;

	public Prediction(Scope scope) {
		assert scope != null :
			"Scope is missing";
		this.scope = scope;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public abstract Predicted getPredicted();

	public final boolean isExact() {
		return getPredicted().isExact();
	}

	public final boolean isPredicted() {
		return getPredicted().isPredicted();
	}

	public final boolean assertEncloses(Scope scope) {
		return getPredicted().assertEnclosingPrediction(this, scope);
	}

}

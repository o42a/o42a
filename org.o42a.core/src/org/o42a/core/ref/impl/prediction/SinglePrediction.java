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

import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;
import org.o42a.util.collect.ReadonlyIterator;


public final class SinglePrediction extends Prediction {

	private final Prediction basePrediction;
	private final Pred pred;

	public SinglePrediction(Prediction basePrediction, Pred pred) {
		super(pred.getScope());
		this.basePrediction = basePrediction;
		this.pred = pred;
	}

	@Override
	public final Predicted getPredicted() {
		return this.basePrediction.getPredicted();
	}

	@Override
	public final ReadonlyIterator<Pred> iterator() {
		return this.pred.iterator();
	}

	@Override
	public String toString() {
		if (this.pred == null) {
			return super.toString();
		}
		return this.pred.toString();
	}

}

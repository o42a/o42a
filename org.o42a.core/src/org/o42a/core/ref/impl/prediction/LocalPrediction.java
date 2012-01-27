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

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;


public class LocalPrediction extends Prediction {

	public static Prediction predictLocal(
			Prediction enclosing,
			LocalScope local) {
		assert enclosing.assertEncloses(local);

		switch (enclosing.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(local);
		case UNPREDICTED:
			return unpredicted(local);
		case PREDICTED:
			return new LocalPrediction(local);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + enclosing.getPredicted());
	}

	private LocalPrediction(LocalScope local) {
		super(local);
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Scope> iterator() {
		return singletonList(getScope()).iterator();
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "LocalPrediction[" + scope + ']';
	}

}

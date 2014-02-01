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

import org.o42a.core.Scope;
import org.o42a.core.ref.Pred;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;
import org.o42a.util.collect.ReadonlyIterator;


public class Unpredicted extends Prediction {

	public Unpredicted(Scope scope) {
		super(scope);
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.UNPREDICTED;
	}

	@Override
	public ReadonlyIterator<Pred> iterator() {
		return noPred().iterator();
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope + "?";
	}

}

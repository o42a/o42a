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

import static org.o42a.util.collect.Iterators.singletonIterator;

import org.o42a.core.Scope;
import org.o42a.core.ref.impl.prediction.InitialPrediction;
import org.o42a.core.ref.impl.prediction.Unpredicted;
import org.o42a.util.collect.ReadonlyIterator;


public abstract class Pred {

	private static final NoPred NO_PRED = new NoPred();

	public static Pred noPred() {
		return NO_PRED;
	}

	private final Scope scope;

	public Pred(Scope scope) {
		this.scope = scope;
	}

	public final boolean isPredicted() {
		return getScope() != null;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final Pred setScope(Scope scope) {
		if (!isPredicted()) {
			return this;
		}
		if (scope.is(getScope())) {
			return this;
		}
		scope.assertDerivedFrom(getScope());
		return new AnotherScopePred(scope, this);
	}

	public final ReadonlyIterator<Pred> iterator() {
		return singletonIterator(this);
	}

	public final Scope revert() {
		return revert(getScope());
	}

	public final Prediction toScopePrediction() {
		if (!isPredicted()) {
			return new Unpredicted(getScope());
		}
		return new InitialPrediction(getScope());
	}

	@Override
	public String toString() {
		if (this.scope == null) {
			return super.toString();
		}
		return this.scope.toString();
	}

	protected abstract Scope revert(Scope scope);

	private static final class NoPred extends Pred {

		NoPred() {
			super(null);
		}

		@Override
		public String toString() {
			return "NoPred";
		}

		@Override
		protected Scope revert(Scope scope) {
			return null;
		}

	}

	private static final class AnotherScopePred extends Pred {

		private final Pred pred;

		AnotherScopePred(Scope scope, Pred pred) {
			super(scope);
			this.pred = pred;
		}

		@Override
		public String toString() {
			if (this.pred == null) {
				return super.toString();
			}
			return this.pred + "{" + getScope() + "}";
		}

		@Override
		protected Scope revert(Scope scope) {
			return this.pred.revert(scope);
		}

	}

}

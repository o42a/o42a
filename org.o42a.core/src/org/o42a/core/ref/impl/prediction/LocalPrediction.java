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

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.*;


public class LocalPrediction extends Prediction {

	private final Prediction basePrediction;

	public static Prediction predictLocal(
			Prediction basePrediction,
			LocalScope local) {
		assert basePrediction.assertEncloses(local);

		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(basePrediction, local);
		case UNPREDICTED:
			return unpredicted(local);
		case PREDICTED:
			return new LocalPrediction(basePrediction, local);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	private LocalPrediction(Prediction basePrediction, LocalScope local) {
		super(local);
		this.basePrediction = basePrediction;
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Pred> iterator() {
		return new Itr(this);
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "LocalPrediction[" + scope + ']';
	}

	private static final class Itr implements Iterator<Pred> {

		private final Iterator<Pred> bases;
		private final MemberKey key;

		public Itr(LocalPrediction prediction) {
			this.bases = prediction.basePrediction.iterator();
			this.key = prediction.getScope().toLocal().toMember().getMemberKey();
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

			final LocalScope local =
					base.getScope()
					.toObject()
					.member(this.key)
					.toLocal()
					.local();

			return new LocalPred(base, local);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class LocalPred extends DerivedPred {

		LocalPred(Pred base, Scope scope) {
			super(base, scope);
		}

		@Override
		protected Scope baseOf(Scope derived) {
			return derived.toLocal().getOwner().getScope();
		}

	}

}

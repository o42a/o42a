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
import static org.o42a.core.ref.impl.prediction.ObjectImplementations.objectImplementations;
import static org.o42a.util.collect.Iterators.combineIterators;
import static org.o42a.util.collect.Iterators.singletonIterator;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldReplacement;
import org.o42a.core.ref.*;
import org.o42a.util.collect.SubIterator;


public class FieldPrediction extends Prediction {

	public static Prediction predictField(
			Prediction basePrediction,
			Field field) {
		assert basePrediction.assertEncloses(field);

		switch (basePrediction.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(basePrediction, field);
		case UNPREDICTED:
			return unpredicted(field);
		case PREDICTED:
			return new FieldPrediction(basePrediction, field);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + basePrediction.getPredicted());
	}

	private final Prediction basePrediction;

	private FieldPrediction(Prediction basePrediction, Field field) {
		super(field);
		this.basePrediction = basePrediction;
		field.getEnclosingScope().assertDerivedFrom(basePrediction.getScope());
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Pred> iterator() {
		return new Itr(this.basePrediction, getScope().toField());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "FieldPrediction[" + scope + ']';
	}

	private static final class Itr extends SubIterator<Pred, Pred> {

		private final MemberKey fieldKey;

		Itr(Prediction basePrediction, Field field) {
			super(basePrediction.iterator());
			this.fieldKey = field.getKey();
		}

		@Override
		public String toString() {
			if (this.fieldKey == null) {
				return super.toString();
			}
			return "FieldIterator[" + this.fieldKey + ']';
		}

		@Override
		protected Iterator<? extends Pred> nestedIterator(Pred nextBase) {
			if (!nextBase.isPredicted()) {
				return nextBase.iterator();
			}
			return new OverridersIterator(
					nextBase,
					nextBase.getScope()
					.getContainer()
					.member(this.fieldKey)
					.toField()
					.field(dummyUser()));
		}

	}

	private static final class OverridersIterator
			extends SubIterator<Pred, Field> {

		private final Pred base;
		private final Field start;

		OverridersIterator(Pred base, Field start) {
			super(combineIterators(
					singletonIterator(start),
					new ReplacementsIterator(start)));
			this.start = start;
			this.base = base;
			start.getEnclosingScope().assertDerivedFrom(base.getScope());
		}

		@Override
		public String toString() {
			if (this.start == null) {
				return super.toString();
			}
			return "OverridersIterator[" + this.start + ']';
		}

		@Override
		protected Iterator<? extends Pred> nestedIterator(Field field) {
			return objectImplementations(new FieldPred(this.base, field));
		}

	}

	private static final class ReplacementsIterator
			extends SubIterator<Field, FieldReplacement> {

		private final Field start;

		ReplacementsIterator(Field start) {
			super(start.toMember().allReplacements().iterator());
			this.start = start;
		}

		@Override
		protected Iterator<? extends Field> nestedIterator(
				FieldReplacement replacement) {

			final Field replacingField =
					replacement.toField().field(dummyUser());

			return combineIterators(
					singletonIterator(replacingField),
					new ReplacementsIterator(replacingField));
		}

		@Override
		public String toString() {
			if (this.start == null) {
				return super.toString();
			}
			return "ReplacementsIterator[" + this.start + ']';
		}

	}

	private static final class FieldPred extends DerivedPred {

		FieldPred(Pred base, Field field) {
			super(base, field);
		}

		@Override
		protected Scope baseOf(Scope derived) {

			final MemberKey expectedKey = getScope().toField().getKey();
			final Field derivedField = derived.toField();

			if (derivedField != null
					&& derivedField.getKey().equals(expectedKey)) {
				return derivedField.getEnclosingScope();
			}

			return expectedKey.getOrigin();
		}

	}

}

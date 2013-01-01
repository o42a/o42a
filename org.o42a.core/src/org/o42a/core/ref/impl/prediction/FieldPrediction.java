/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldReplacement;
import org.o42a.core.ref.*;


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

	private static final class Itr implements Iterator<Pred> {

		private final Prediction basePrediction;
		private final Iterator<Pred> bases;
		private final MemberKey fieldKey;
		private Iterator<Pred> overriders;

		Itr(Prediction basePrediction, Field field) {
			this.basePrediction = basePrediction;
			this.fieldKey = field.getKey();
			this.bases = basePrediction.iterator();
		}

		@Override
		public boolean hasNext() {
			if (this.overriders == null || !this.overriders.hasNext()) {
				return findNext();
			}
			return this.overriders.hasNext();
		}

		@Override
		public Pred next() {
			if (this.overriders == null || !this.overriders.hasNext()) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}
			return this.overriders.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			if (this.fieldKey == null) {
				return super.toString();
			}
			return "FieldIterator[" + this.fieldKey + ']';
		}

		private boolean findNext() {
			do {
				if (!this.bases.hasNext()) {
					return false;
				}

				final Pred nextBase = this.bases.next();

				if (!nextBase.isPredicted()) {
					this.overriders = nextBase.iterator();
					return true;
				}

				this.overriders = new OverridersItr(
						this.basePrediction,
						nextBase,
						nextBase.getScope()
						.getContainer()
						.member(this.fieldKey)
						.toField()
						.field(dummyUser()));
			} while (this.overriders == null || !this.overriders.hasNext());
			return true;
		}

	}

	private static final class OverridersItr implements Iterator<Pred> {

		private final Prediction basePrediction;
		private final Pred base;
		private final ReplacementsItr replacements;
		private Iterator<Pred> impls;

		OverridersItr(Prediction basePrediction, Pred base, Field start) {
			this.basePrediction = basePrediction;
			this.base = base;
			this.replacements = new ReplacementsItr(start);
			start.getEnclosingScope().assertDerivedFrom(
					basePrediction.getScope());
		}

		@Override
		public boolean hasNext() {
			if (this.impls == null || !this.impls.hasNext()) {
				return findNext();
			}
			return true;
		}

		@Override
		public Pred next() {
			if (this.impls == null || !this.impls.hasNext()) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}
			return this.impls.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			if (this.replacements == null) {
				return super.toString();
			}
			return "OverridersIterator[" + this.replacements.start + ']';
		}

		private boolean findNext() {
			do {
				if (!this.replacements.hasNext()) {
					return false;
				}

				final Field field = this.replacements.next();
				final Prediction basePrediction = new SinglePrediction(
						this.basePrediction,
						this.base.setScope(field.getEnclosingScope()));
				final ObjectImplementations impls =
						new ObjectImplementations(
								basePrediction,
								new FieldPred(this.base, field));

				this.impls = impls.iterator();
			} while (this.impls == null || !this.impls.hasNext());

			return true;
		}

	}

	private static final class ReplacementsItr implements Iterator<Field> {

		private final Field start;
		private Iterator<FieldReplacement> replacements;
		private ReplacementsItr sub;

		ReplacementsItr(Field start) {
			this.start = start;
		}

		@Override
		public boolean hasNext() {
			if (this.replacements == null) {
				return true;
			}
			if (this.sub != null && this.sub.hasNext()) {
				return true;
			}
			return this.replacements.hasNext();
		}

		@Override
		public Field next() {
			if (this.replacements == null) {
				this.replacements =
						this.start.toMember().allReplacements().iterator();
				return this.start;
			}
			if (this.sub == null || !this.sub.hasNext()) {

				final FieldReplacement replacement = this.replacements.next();

				this.sub = new ReplacementsItr(
						replacement.toField().field(dummyUser()));
			}

			return this.sub.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
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

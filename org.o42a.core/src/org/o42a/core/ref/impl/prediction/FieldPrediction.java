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

import static org.o42a.analysis.use.User.dummyUser;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldReplacement;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;


public class FieldPrediction extends Prediction {

	private final Prediction enclosing;

	public static Prediction predictField(
			Prediction enclosing,
			Field<?> field) {
		assert enclosing.assertEncloses(field);

		if (field.getArtifactKind().isVariable()) {
			// Variables can not be predicted for now.
			return unpredicted(field);
		}

		switch (enclosing.getPredicted()) {
		case EXACTLY_PREDICTED:
			return exactPrediction(field);
		case UNPREDICTED:
			return unpredicted(field);
		case PREDICTED:
			return new FieldPrediction(enclosing, field);
		}

		throw new IllegalArgumentException(
				"Unsupported prediction: " + enclosing.getPredicted());
	}

	private FieldPrediction(Prediction enclosing, Field<?> field) {
		super(field);
		this.enclosing = enclosing;
		field.getEnclosingScope().assertDerivedFrom(enclosing.getScope());
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Scope> iterator() {
		return new Itr(this.enclosing, getScope().toField());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "FieldPrediction[" + scope + ']';
	}

	private static final class Itr implements Iterator<Scope> {

		private final Prediction enclosing;
		private final Iterator<Scope> enclosings;
		private final MemberKey fieldKey;
		private OverridersItr overriders;

		Itr(Prediction enclosing, Field<?> field) {
			this.enclosing = enclosing;
			this.fieldKey = field.getKey();
			this.enclosings = enclosing.iterator();
		}

		@Override
		public boolean hasNext() {
			if (this.overriders == null || !this.overriders.hasNext()) {
				return findNext();
			}
			return this.overriders.hasNext();
		}

		@Override
		public Scope next() {
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
				if (!this.enclosings.hasNext()) {
					return false;
				}
				this.overriders = new OverridersItr(
						this.enclosing,
						this.enclosings.next()
						.getScope()
						.getContainer()
						.member(this.fieldKey)
						.toField()
						.field(dummyUser()));
			} while (this.overriders == null || !this.overriders.hasNext());
			return true;
		}

	}

	private static final class OverridersItr implements Iterator<Scope> {

		private final Prediction enclosing;
		private final ReplacementsItr replacements;
		private Iterator<Scope> impls;

		OverridersItr(Prediction enclosing, Field<?> start) {
			this.enclosing = enclosing;
			this.replacements = new ReplacementsItr(start);
			start.getEnclosingScope().assertDerivedFrom(enclosing.getScope());
		}

		@Override
		public boolean hasNext() {
			if (this.impls == null || !this.impls.hasNext()) {
				return findNext();
			}
			return true;
		}

		@Override
		public Scope next() {
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

				final Field<?> field = this.replacements.next();
				final Prediction enclosing;

				if (this.enclosing.isExact()) {
					enclosing = exactPrediction(field.getEnclosingScope());
				} else {
					enclosing = new SimplePrediction(field.getEnclosingScope());
				}

				final ObjectImplementations impls = new ObjectImplementations(
						enclosing,
						field.getArtifact().materialize());

				this.impls = impls.iterator();
			} while (this.impls == null || !this.impls.hasNext());

			return true;
		}

	}

	private static final class ReplacementsItr implements Iterator<Field<?>> {

		private final Field<?> start;
		private Iterator<FieldReplacement> replacements;
		private ReplacementsItr sub;

		ReplacementsItr(Field<?> start) {
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
		public Field<?> next() {
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

}

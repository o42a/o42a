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

import static org.o42a.util.use.User.dummyUser;

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
	}

	@Override
	public Predicted getPredicted() {
		return Predicted.PREDICTED;
	}

	@Override
	public Iterator<Scope> iterator() {
		return new Iter(this.enclosing, getScope().toField());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "FieldPrediction[" + scope + ']';
	}

	private static final class Iter implements Iterator<Scope> {

		private final Prediction enclosing;
		private final Iterator<Scope> enclosings;
		private final MemberKey fieldKey;
		private EIter replacements;

		Iter(Prediction enclosing, Field<?> field) {
			this.enclosing = enclosing;
			this.fieldKey = field.getKey();
			this.enclosings = enclosing.iterator();
		}

		@Override
		public boolean hasNext() {
			if (this.replacements != null && this.replacements.hasNext()) {
				return true;
			}
			return this.enclosings.hasNext();
		}

		@Override
		public Scope next() {
			if (this.replacements == null || !this.replacements.hasNext()) {
				this.replacements = new EIter(
						this.enclosing,
						this.enclosings.next()
						.getScope()
						.getContainer()
						.member(this.fieldKey)
						.toField()
						.field(dummyUser()));
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class EIter implements Iterator<Scope> {

		private final Prediction enclosing;
		private final ReplacementsIterator replacements;
		private Iterator<Scope> impls;

		EIter(Prediction enclosing, Field<?> start) {
			this.enclosing = enclosing;
			this.replacements = new ReplacementsIterator(start);
		}

		@Override
		public boolean hasNext() {
			if (this.impls == null || !this.impls.hasNext()) {
				return nextImpl();
			}
			return true;
		}

		@Override
		public Scope next() {
			if (this.impls == null || !this.impls.hasNext()) {
				if (!nextImpl()) {
					throw new NoSuchElementException();
				}
			}
			return this.impls.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private boolean nextImpl() {
			do {
				if (!this.replacements.hasNext()) {
					return false;
				}

				final Field<?> field = this.replacements.next();
				final ObjectImplementations impls = new ObjectImplementations(
						this.enclosing,
						field.getArtifact().materialize());

				this.impls = impls.iterator();
			} while (this.impls == null || !this.impls.hasNext());

			return true;
		}

	}

	private static final class ReplacementsIterator
			implements Iterator<Field<?>> {

		private final Field<?> start;
		private Iterator<FieldReplacement> replacements;
		private ReplacementsIterator sub;

		ReplacementsIterator(Field<?> start) {
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

				this.sub = new ReplacementsIterator(
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

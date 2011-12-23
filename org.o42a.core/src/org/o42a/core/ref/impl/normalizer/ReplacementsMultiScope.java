/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref.impl.normalizer;

import static org.o42a.util.use.User.dummyUser;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldReplacement;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


public final class ReplacementsMultiScope extends MultiScope {

	public static MultiScope replacementMultiScope(Field<?> field) {
		if (field.toMember().allReplacements().isEmpty()) {
			return new PropagatedMultiScope(field);
		}
		return new ReplacementsMultiScope(field);
	}

	private ReplacementsMultiScope(Field<?> field) {
		super(field);
	}

	@Override
	public final MultiScopeSet getScopeSet() {
		return MultiScopeSet.MULTI_SCOPE;
	}

	@Override
	public Iterator<Scope> iterator() {
		return new ReplacementsIterator(getScope().toField());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return "DerivativesMultiScope[" + scope.toField() + ']';
	}

	static final class ReplacementsIterator implements Iterator<Scope> {

		private final Field<?> start;
		private Iterator<FieldReplacement> replacements;
		private Iterator<Scope> sub;

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
		public Scope next() {
			if (this.replacements == null) {
				this.replacements =
						this.start.toMember().allReplacements().iterator();
				return this.start.getScope();
			}
			if (this.sub == null || !this.sub.hasNext()) {

				final FieldReplacement replacement = this.replacements.next();
				final Field<?> field = replacement.toField().field(dummyUser());

				if (field != null) {
					this.sub = new ReplacementsIterator(field);
				} else {
					this.sub = null;
				}
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
			return "DerivativesIterator[" + this.start + ']';
		}

	}

}

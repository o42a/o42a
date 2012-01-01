/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


final class MultiOwnerReplacementsMultiScope extends MultiScope {

	private final MultiScope owner;

	MultiOwnerReplacementsMultiScope(MultiScope owner, Field<?> field) {
		super(field);
		this.owner = owner;
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return MultiScopeSet.SCOPES;
	}

	@Override
	public Iterator<Scope> iterator() {
		return new ReplacementsIterator();
	}

	@Override
	public String toString() {
		if (this.owner == null) {
			return super.toString();
		}
		return "ReplacementsMultiScope[" + getKey() + " in " + this.owner + ']';
	}

	private final MemberKey getKey() {
		return getScope().toField().getKey();
	}

	private final class ReplacementsIterator implements Iterator<Scope> {

		private final Iterator<Scope> owners =
				MultiOwnerReplacementsMultiScope.this.owner.iterator();
		private Iterator<Scope> replacements;

		@Override
		public boolean hasNext() {
			if (this.replacements != null && this.replacements.hasNext()) {
				return true;
			}
			return this.owners.hasNext();
		}

		@Override
		public Scope next() {
			if (this.replacements == null || !this.replacements.hasNext()) {
				this.replacements =
						new ReplacementsMultiScope.ReplacementsIterator(
								this.owners.next()
								.getContainer()
								.member(getKey())
								.toField()
								.field(dummyUser()));
			}
			return this.replacements.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return ("ReplacementsIterator[" + getKey()
					+ " in " + MultiOwnerReplacementsMultiScope.this.owner
					+ ']');
		}

	}

}

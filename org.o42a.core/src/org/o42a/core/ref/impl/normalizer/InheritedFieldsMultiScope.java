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

import static java.util.Collections.singletonList;
import static org.o42a.util.use.User.dummyUser;

import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.MultiScope;
import org.o42a.core.ref.MultiScopeSet;


final class InheritedFieldsMultiScope extends MultiScope {

	private final InheritedFields ancestors;

	InheritedFieldsMultiScope(
			Field<?> field,
			Iterable<Scope> ancestors) {
		super(field);
		this.ancestors = new InheritedFields(field.getKey(), ancestors);
	}

	@Override
	public MultiScopeSet getScopeSet() {
		return MultiScopeSet.INHERITED_SCOPES;
	}

	@Override
	public Iterable<Scope> ancestors() {
		return this.ancestors;
	}

	@Override
	public Iterator<Scope> iterator() {
		return singletonList(getScope()).iterator();
	}

	@Override
	public String toString() {
		if (this.ancestors == null) {
			return super.toString();
		}
		return ("InheritedFieldsMultiScope[" + getScope().toField().getKey()
				+ " from " + this.ancestors.ancestors + ']');
	}

	private static final class InheritedFields implements Iterable<Scope> {

		private final MemberKey memberKey;
		private final Iterable<Scope> ancestors;

		InheritedFields(MemberKey memberKey, Iterable<Scope> ancestors) {
			this.memberKey = memberKey;
			this.ancestors = ancestors;
		}

		@Override
		public Iterator<Scope> iterator() {
			return new InheritedFieldsIterator(this.memberKey, this.ancestors);
		}

		@Override
		public String toString() {
			if (this.ancestors == null) {
				return super.toString();
			}
			return ("InheritedFields[" + this.memberKey
					+ " from " + this.ancestors + ']');
		}

	}

	private static final class InheritedFieldsIterator
			implements Iterator<Scope> {

		private final MemberKey memberKey;
		private final Iterator<Scope> ancestors;

		InheritedFieldsIterator(
				MemberKey memberKey,
				Iterable<Scope> ancestors) {
			this.memberKey = memberKey;
			this.ancestors = ancestors.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.ancestors.hasNext();
		}

		@Override
		public Scope next() {

			final Scope ancestor = this.ancestors.next();
			final Member member =
					ancestor.getContainer().member(this.memberKey);

			return member.toField().field(dummyUser());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			if (this.ancestors == null) {
				return super.toString();
			}
			return ("InheritedFields[" + this.memberKey
					+ " from " + this.ancestors + ']');
		}

	}

}

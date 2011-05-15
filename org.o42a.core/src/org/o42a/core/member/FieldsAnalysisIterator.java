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
package org.o42a.core.member;

import static org.o42a.util.use.User.dummyUser;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.member.field.Field;


final class FieldsAnalysisIterator implements Iterator<MemberAnalysis> {

	private final MemberContainer container;
	private final Iterator<? extends Member> members;
	private MemberAnalysis next;

	FieldsAnalysisIterator(MemberContainer container) {
		this.container = container;
		this.members = container.getMembers().iterator();
		this.next = findNext();
	}

	@Override
	public boolean hasNext() {
		return this.next != null;
	}

	@Override
	public MemberAnalysis next() {

		final MemberAnalysis next = this.next;

		if (next == null) {
			throw new NoSuchElementException();
		}
		this.next = findNext();

		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (this.container == null) {
			return super.toString();
		}
		return "FieldsAnalysisIterator[" + this.container + ']';
	}

	private MemberAnalysis findNext() {
		while (this.members.hasNext()) {

			final Member member = this.members.next();
			final Field<?> field = member.toField(dummyUser());

			if (field == null) {
				continue;
			}

			return member.getAnalysis();
		}

		return null;
	}

}

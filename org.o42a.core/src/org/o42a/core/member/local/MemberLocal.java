/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.local;

import org.o42a.core.*;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.field.Field;


abstract class MemberLocal extends Member {

	MemberLocal(LocationInfo location, Distributor distributor, Obj owner) {
		super(location, distributor);
	}

	@Override
	public final Field<?> toField() {
		return null;
	}

	@Override
	public final Clause toClause() {
		return toLocal().toClause();
	}

	@Override
	public final Container getSubstance() {
		return toLocal();
	}

	@Override
	public final Visibility getVisibility() {

		final Clause clause = toClause();

		if (clause != null) {
			return Visibility.PUBLIC;
		}

		return Visibility.PRIVATE;
	}

	@Override
	public final boolean isOverride() {
		return isPropagated();
	}

	@Override
	public final boolean isAbstract() {
		return false;
	}

	@Override
	public final Member propagateTo(Scope scope) {

		final Obj owner = scope.getContainer().toObject();

		assert owner != null :
			scope + " is not object";

		return toLocal().propagateTo(owner).toMember();
	}

	@Override
	public void resolveAll() {
	}

	@Override
	public Member wrap(Member inherited, Container container) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void merge(Member member) {
		throw new UnsupportedOperationException();
	}

}

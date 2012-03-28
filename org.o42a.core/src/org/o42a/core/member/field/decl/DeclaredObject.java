/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.member.field.decl;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;


class DeclaredObject extends Obj {

	private final DeclaredField field;

	DeclaredObject(DeclaredField field) {
		super(field);
		this.field = field;
	}

	@Override
	public boolean isValid() {
		return this.field.validate();
	}

	@Override
	public String toString() {
		return this.field != null ? this.field.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		this.field.initDefinitions(this);
		return new Ascendants(this).declareField(this.field);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.field.getMemberRegistry().registerMembers(members);
	}

	@Override
	protected void updateMembers() {
		this.field.updateMembers();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.field.define(getScope());
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {

		final MemberField field =
				enclosing.getContainer().member(this.field.getKey()).toField();

		return field.artifact(dummyUser()).materialize();
	}

}

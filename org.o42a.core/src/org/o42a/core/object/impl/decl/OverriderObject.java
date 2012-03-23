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
package org.o42a.core.object.impl.decl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.type.FieldAscendants.NO_FIELD_ASCENDANTS;

import org.o42a.core.Scope;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;


final class OverriderObject extends Obj {

	private final DeclaredObjectField field;

	OverriderObject(DeclaredObjectField field) {
		super(field);
		this.field = field;
	}

	@Override
	public String toString() {
		return this.field != null ? this.field.toString() : super.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return this.field.updateAscendants(
				new Ascendants(this).declareField(NO_FIELD_ASCENDANTS));
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

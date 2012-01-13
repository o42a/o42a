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
package org.o42a.core.artifact.object.impl.decl;

import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;


public final class PropagatedObject extends Obj {

	public PropagatedObject(Field<Obj> field) {
		super(field, field.getOverridden()[0].getArtifact());
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).declareMember();
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {

		final MemberField field = enclosing.getContainer().member(
				getScope().toField().getKey()).toField();

		return field.field(dummyUser()).getArtifact().materialize();
	}

}

/*
    Modules Commons
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
package org.o42a.common.intrinsic;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.value.ValueType;


public class IntrinsicType extends IntrinsicObject {

	public IntrinsicType(
			Container enclosingContainer,
			String name,
			ValueType<?> valueType) {
		super(
				fieldDeclaration(
						enclosingContainer,
						enclosingContainer.getScope().distribute(),
						memberName(name))
				.prototype());
		setValueType(valueType);
	}

	public IntrinsicType(FieldDeclaration declaration, ValueType<?> valueType) {
		super(declaration);
		setValueType(valueType);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(getScope()).setAncestor(
				ValueType.VOID.typeRef(this, getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ancestorDefinitions) {
		return getValueType().noValueDefinitions(this, scope);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getValueType().noValueDefinitions(this, getScope());
	}

}

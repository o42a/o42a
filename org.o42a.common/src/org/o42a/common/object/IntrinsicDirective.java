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
package org.o42a.common.object;

import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.Directive;
import org.o42a.core.value.ValueType;


@Deprecated
public abstract class IntrinsicDirective
		extends IntrinsicObject
		implements Directive {

	public IntrinsicDirective(MemberOwner owner, FieldDeclaration declarator) {
		super(owner, declarator);
		setValueType(ValueType.DIRECTIVE);
	}

	@Override
	protected Ascendants createAscendants() {
		return new Ascendants(this).setAncestor(
				ValueType.DIRECTIVE.typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref ref =
				ValueType.DIRECTIVE.constantRef(this, distribute(), this);

		return ref.toValueDef().toDefinitions();
	}

}

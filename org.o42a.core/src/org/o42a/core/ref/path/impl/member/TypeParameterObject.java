/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.ref.path.impl.member;

import static org.o42a.core.object.ConstructionMode.DYNAMIC_CONSTRUCTION;

import org.o42a.core.member.MemberKey;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.macro.MacroDef;


final class TypeParameterObject extends Obj {

	private final TypeParameterConstructor constructor;

	TypeParameterObject(TypeParameterConstructor constructor) {
		super(constructor, constructor.distribute());
		this.constructor = constructor;
		setValueType(ValueType.MACRO);
	}

	public final MemberKey getParameterKey() {
		return this.constructor.getParameterKey();
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return DYNAMIC_CONSTRUCTION;
	}

	@Override
	protected Nesting createNesting() {
		return this.constructor.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this)
		.setAncestor(this.constructor.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new MacroDef(this, this, new TypeParameterMacro(this))
		.toDefinitions(type().getParameters());
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

}

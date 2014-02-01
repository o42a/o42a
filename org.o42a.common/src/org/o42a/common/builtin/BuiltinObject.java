/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.common.builtin;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.ObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


public abstract class BuiltinObject extends Obj implements Builtin {

	public BuiltinObject(
			LocationInfo location,
			Distributor enclosing,
			ValueType<?> valueType) {
		super(location, enclosing);
		setValueType(valueType);
	}

	protected BuiltinObject(ObjectScope scope, ValueType<?> valueType) {
		super(scope);
		setValueType(valueType);
	}

	@Override
	public boolean isConstantBuiltin() {
		return false;
	}

	@Override
	public TypeParameters<?> getBuiltinTypeParameters() {
		return type().getParameters();
	}

	@Override
	public abstract String toString();

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				type().getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new BuiltinDef(this).toDefinitions(type().getParameters());
	}

}

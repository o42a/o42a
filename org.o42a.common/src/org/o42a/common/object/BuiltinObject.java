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

import org.o42a.common.def.Builtin;
import org.o42a.common.def.BuiltinValueDef;
import org.o42a.common.ir.BuiltinValueIR;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.artifact.object.ObjectScope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueType;


public abstract class BuiltinObject extends PlainObject implements Builtin {

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

	protected BuiltinObject(Scope scope, ValueType<?> valueType) {
		super(scope);
		setValueType(valueType);
	}

	@Override
	public abstract String toString();

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				value().getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected final Definitions explicitDefinitions() {
		return new BuiltinValueDef(this).toDefinitions();
	}

	@Override
	protected BuiltinValueIR createValueIR(ObjectIR objectIR) {
		return new BuiltinValueIR(this, objectIR);
	}

}

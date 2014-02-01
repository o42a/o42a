/*
    Compiler Core
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
package org.o42a.core.value.impl;

import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.core.Distributor;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.ConstructedObject;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.Value;


final class ConstantObject<T> extends ConstructedObject {

	private final Value<T> value;

	ConstantObject(
			Constant<T> constant,
			Distributor enclosing,
			SingleValueType<T> valueType,
			T value) {
		super(constant, enclosing);
		setValueType(valueType);
		this.value = typeParameters(this, valueType).compilerValue(value);
	}

	public final Value<T> getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.value.toString();
	}

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
		return new ConstantDef<>(this).toDefinitions(type().getParameters());
	}

}

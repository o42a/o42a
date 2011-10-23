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
package org.o42a.core.value.impl;

import org.o42a.core.Distributor;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.SingleValueType;
import org.o42a.core.value.Value;


public final class ConstantObject<T> extends Obj {

	private final Value<T> value;

	public ConstantObject(
			LocationInfo location,
			Distributor enclosing,
			SingleValueType<T> valueType,
			T value) {
		super(location, enclosing);
		setValueStruct(valueType.struct());
		this.value = valueType.constantValue(value);
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
				value().getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new ConstantValueDef<T>(this).toDefinitions();
	}

}

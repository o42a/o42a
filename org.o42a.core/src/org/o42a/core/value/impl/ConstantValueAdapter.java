/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.def.Def.sourceOf;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.ValueDef;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.*;


final class ConstantValueAdapter<T> extends ValueAdapter {

	private final LocationInfo location;
	private final Scope scope;
	private final SingleValueType<T> valueType;
	private final T constant;

	ConstantValueAdapter(
			LocationInfo location,
			Scope scope,
			SingleValueType<T> valueType,
			T constant) {
		this.location = location;
		this.scope = scope;
		this.valueType = valueType;
		this.constant = constant;
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return this.valueType.struct();
	}

	@Override
	public ValueDef valueDef() {
		return this.valueType.struct().constantDef(
				sourceOf(this.scope),
				this.location,
				this.constant);
	}

	@Override
	public CondDef condDef() {
		return logicalTrue(this.location, this.scope).toCondDef();
	}

	@Override
	public Logical logical(Scope scope) {
		return logicalTrue(this.location, scope);
	}

	@Override
	public Value<?> initialValue(LocalResolver resolver) {
		return this.valueType.constantValue(this.constant);
	}

	@Override
	public LogicalValue initialLogicalValue(LocalResolver resolver) {
		return LogicalValue.TRUE;
	}

	@Override
	public String toString() {
		if (this.constant == null) {
			return "null";
		}
		return this.valueType.struct().valueString(this.constant);
	}
}

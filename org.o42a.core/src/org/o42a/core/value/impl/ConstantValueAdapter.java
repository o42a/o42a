/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.object.def.Def.sourceOf;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.*;


final class ConstantValueAdapter<T> extends ValueAdapter {

	private final SingleValueType<T> valueType;
	private final T constant;

	ConstantValueAdapter(
			Ref adaptedRef,
			SingleValueType<T> valueType,
			T constant) {
		super(adaptedRef);
		this.valueType = valueType;
		this.constant = constant;
	}

	@Override
	public Def valueDef() {
		return this.valueType.struct().constantDef(
				sourceOf(getAdaptedRef()),
				getAdaptedRef(),
				this.constant);
	}

	@Override
	public Logical logical(Scope scope) {
		return logicalTrue(getAdaptedRef(), scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return this.valueType.constantValue(this.constant);
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
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

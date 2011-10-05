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
import org.o42a.core.value.*;


public class ConstantValueAdapter<T> extends ValueAdapter {

	private ConstantRef<T> ref;

	public ConstantValueAdapter(ConstantRef<T> ref) {
		this.ref = ref;
	}

	public final ConstantRef<T> ref() {
		return this.ref;
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return ref().valueStruct(scope);
	}

	@Override
	public ValueDef valueDef() {

		final Value<T> value =
				ref().getValueStruct().constantValue(ref().getConstant());

		return new ConstantValueDef<T>(sourceOf(ref()), ref(), value);
	}

	@Override
	public CondDef condDef() {
		return logicalTrue(this.ref, this.ref.getScope()).toCondDef();
	}

	@Override
	public Logical logical(Scope scope) {
		return ref().rescope(scope).getLogical();
	}

	@Override
	public Value<?> initialValue(LocalResolver resolver) {
		return ref().value(resolver);
	}

	@Override
	public LogicalValue initialLogicalValue(LocalResolver resolver) {
		return ref().value(resolver).getCondition().toLogicalValue();
	}

}

/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.array;

import static org.o42a.core.object.def.Def.sourceOf;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.array.ArrayValueType;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;


final class ArrayInitValueAdapter extends ValueAdapter {

	private final ArrayConstructor constructor;
	private final ArrayValueStruct arrayStruct;

	ArrayInitValueAdapter(
			Ref adaptedRef,
			ArrayConstructor constructor,
			ArrayValueStruct arrayStruct) {
		super(adaptedRef);
		this.constructor = constructor;
		this.arrayStruct = arrayStruct;
	}

	@Override
	public Def valueDef() {

		final Scope scope = getAdaptedRef().getScope();
		final Array array = createArray(scope);

		return array.getValueStruct().constantDef(
				sourceOf(scope),
				this.constructor,
				array);
	}

	@Override
	public Logical logical(Scope scope) {
		return logicalTrue(this.constructor, scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {

		final Array array = createArray(resolver.getScope());

		return array.getValueStruct().compilerValue(array);
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
		return LogicalValue.TRUE;
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	private Array createArray(Scope scope) {
		return new Builder(this).createArray(
				this.constructor.distributeIn(scope.getEnclosingContainer()),
				scope);
	}

	private static final class Builder extends ArrayBuilder {

		private final ArrayInitValueAdapter adapter;

		Builder(ArrayInitValueAdapter adapter) {
			super(adapter.constructor);
			this.adapter = adapter;
		}

		@Override
		protected ArrayValueType arrayType() {
			return knownArrayStruct().getValueType();
		}

		@Override
		protected boolean typeByItems() {
			return false;
		}

		@Override
		protected ArrayValueStruct knownArrayStruct() {
			return this.adapter.arrayStruct;
		}

	}

}

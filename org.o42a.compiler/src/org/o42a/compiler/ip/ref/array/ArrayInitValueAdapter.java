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
import org.o42a.core.object.def.CondDef;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.*;


final class ArrayInitValueAdapter extends ValueAdapter {

	private final Ref ref;
	private final ArrayConstructor constructor;
	private final ArrayValueStruct arrayStruct;

	ArrayInitValueAdapter(
			Ref ref,
			ArrayConstructor constructor,
			ArrayValueStruct arrayStruct) {
		this.ref = ref;
		this.constructor = constructor;
		this.arrayStruct = arrayStruct;
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return this.arrayStruct.upgradeScope(scope);
	}

	@Override
	public ValueDef valueDef() {

		final Scope scope = this.ref.getScope();
		final Array array = createArray(scope);

		return array.getValueStruct().constantDef(
				sourceOf(scope),
				this.constructor,
				array);
	}

	@Override
	public CondDef condDef() {
		return logical(this.ref.getScope()).toCondDef();
	}

	@Override
	public Logical logical(Scope scope) {
		return logicalTrue(this.constructor, scope);
	}

	@Override
	public Value<?> initialValue(LocalResolver resolver) {

		final Array array = createArray(resolver.getScope());

		return array.getValueStruct().compilerValue(array);
	}

	@Override
	public LogicalValue initialLogicalValue(LocalResolver resolver) {
		return LogicalValue.TRUE;
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

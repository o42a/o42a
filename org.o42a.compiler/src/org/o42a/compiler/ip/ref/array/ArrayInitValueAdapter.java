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
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.ir.value.array.ArrayValueTypeIR;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.array.ArrayValueType;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
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
	public boolean isConstant() {
		return !this.arrayStruct.getValueType().isRuntimeConstructed();
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
		return createArray(resolver.getScope()).toValue();
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
		return LogicalValue.TRUE;
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public RefEval eval(CodeBuilder builder) {
		return new ArrayInitEval(
				builder,
				getAdaptedRef(),
				createArray(getAdaptedRef().getScope()).toValue());
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		value(resolver).resolveAll(resolver);
	}

	private Array createArray(Scope scope) {
		return new ArrayInitBuilder(this).createArray(
				this.constructor.distributeIn(scope.getEnclosingContainer()),
				scope);
	}

	private static final class ArrayInitBuilder extends ArrayBuilder {

		private final ArrayInitValueAdapter adapter;

		ArrayInitBuilder(ArrayInitValueAdapter adapter) {
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

	private static final class ArrayInitEval extends RefEval {

		private final Value<Array> value;

		ArrayInitEval(CodeBuilder builder, Ref ref, Value<Array> value) {
			super(builder, ref);
			this.value = value;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			// Always TRUE.
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			if (!this.value.getCompilerValue().isVariable()) {
				dirs.returnValue(this.value.op(dirs.getBuilder(), dirs.code()));
				return;
			}

			final Array array = this.value.getCompilerValue();
			final ArrayValueType valueType =
					this.value.getValueType().toArrayType();
			final ArrayValueTypeIR valueTypeIR =
					valueType.ir(dirs.getGenerator());
			final ArrayIR arrayIR = array.ir(valueTypeIR);
			final ObjectOp arrayOp =
					array.getPrefix()
					.write(dirs.dirs(), host)
					.materialize(dirs.dirs());
			final ObjectValFunc constructor =
					arrayIR.getConstructor().op(arrayIR.getId(), dirs.code());

			constructor.call(dirs, arrayOp);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

}

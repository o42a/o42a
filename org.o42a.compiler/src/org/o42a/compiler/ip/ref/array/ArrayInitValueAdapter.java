/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.ScopeUpgrade.upgradeScope;

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.ref.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.core.value.link.TargetResolver;


final class ArrayInitValueAdapter extends ValueAdapter {

	private final ArrayConstructor constructor;
	private final TypeParameters<Array> arrayParameters;
	private Value<Array> value;
	private IdentityHashMap<Scope, Value<Array>> cache;

	ArrayInitValueAdapter(
			Ref adaptedRef,
			ArrayConstructor constructor,
			TypeParameters<Array> arrayParameters) {
		super(adaptedRef);
		this.constructor = constructor;
		this.arrayParameters = arrayParameters;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return array(scope).getTypeParameters();
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return array(resolver.getScope());
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval() {
		return new ArrayInitEval(array(getAdaptedRef().getScope()));
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		value(resolver.getResolver())
		.resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

	private Value<Array> array(Scope scope) {
		if (scope.is(getAdaptedRef().getScope())) {
			if (this.value != null) {
				return this.value;
			}
			return this.value = createArray(scope);
		}
		if (this.cache == null) {
			this.cache = new IdentityHashMap<>();
		} else {

			final Value<Array> cached = this.cache.get(scope);

			if (cached != null) {
				return cached;
			}
		}

		final Value<Array> value =
				array(getAdaptedRef().getScope())
				.prefixWith(upgradeScope(getAdaptedRef(), scope).toPrefix());

		this.cache.put(scope, value);

		return value;
	}

	private Value<Array> createArray(Scope scope) {

		final Array array = new ArrayInitBuilder(this).createArray(
				this.constructor.getAccessRules().distribute(
						this.constructor.distributeIn(
								scope.getEnclosingContainer())),
				scope);

		return array.toValue();
	}

	private static final class ArrayInitBuilder extends ArrayBuilder {

		private final ArrayInitValueAdapter adapter;

		ArrayInitBuilder(ArrayInitValueAdapter adapter) {
			super(adapter.constructor);
			this.adapter = adapter;
		}

		@Override
		protected ArrayValueType arrayType() {
			return knownTypeParameters().getValueType().toArrayType();
		}

		@Override
		protected boolean typeByItems() {
			return false;
		}

		@Override
		protected TypeParameters<Array> knownTypeParameters() {
			return this.adapter.arrayParameters;
		}

	}

	private static final class ArrayInitEval implements Eval {

		private final Value<Array> value;

		ArrayInitEval(Value<Array> value) {
			this.value = value;
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
			final ArrayIR arrayIR =
					array.ir(valueType.irGenerator(dirs.getGenerator()));
			final ObjectOp arrayObject =
					array.getPrefix()
					.write(dirs.dirs(), host)
					.materialize(
							dirs.dirs(),
							tempObjHolder(dirs.getAllocator()));
			final ObjectValFunc constructor =
					arrayIR.getConstructor().op(arrayIR.getId(), dirs.code());

			constructor.call(dirs, arrayObject);
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

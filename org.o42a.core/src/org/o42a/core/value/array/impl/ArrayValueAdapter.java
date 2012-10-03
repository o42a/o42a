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
package org.o42a.core.value.array.impl;

import static org.o42a.core.ir.value.ValCopyFunc.VAL_COPY;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValCopyFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.link.TargetResolver;


public final class ArrayValueAdapter extends ValueAdapter {

	private final ArrayValueStruct expectedStruct;

	public ArrayValueAdapter(Ref adaptedRef, ArrayValueStruct expectedStruct) {
		super(adaptedRef);
		this.expectedStruct = expectedStruct;
	}

	public final ArrayValueStruct getExpectedStruct() {
		return this.expectedStruct;
	}

	@Override
	public boolean isConstant() {
		if (getExpectedStruct().isVariable()) {
			return false;
		}
		return getAdaptedRef().isConstant();
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return value(scope.resolver()).getValueStruct();
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return arrayValue(
				getAdaptedRef(),
				resolver,
				getExpectedStruct().isVariable());
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
		if (fromConstToConst()) {
			return new RefOpEval(getAdaptedRef());
		}
		return new ArrayEval(getAdaptedRef());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

	private boolean fromConstToConst() {
		if (getExpectedStruct().isVariable()) {
			return false;
		}
		return !getAdaptedRef().getValueType().isVariable();
	}

	private static Value<?> arrayValue(
			Ref ref,
			Resolver resolver,
			boolean toVariable) {

		final Resolution arrayResolution = ref.resolve(resolver);

		if (arrayResolution.isError()) {
			return falseValue();
		}

		final Obj arrayObject = arrayResolution.toObject();
		final Value<?> value = arrayObject.value().getValue();
		final ArrayValueStruct sourceStruct =
				(ArrayValueStruct) value.getValueStruct();

		final PrefixPath prefix = ref.getPath().toPrefix(resolver.getScope());
		final ArrayValueStruct resultStruct =
				sourceStruct.setVariable(toVariable).prefixWith(prefix);

		if (value.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultStruct.runtimeValue();
		}
		if (sourceStruct.isVariable()) {
			// Mutable array can not be copied at compile time.
			return resultStruct.runtimeValue();
		}

		final Array array = sourceStruct.cast(value).getCompilerValue();
		final ArrayItem[] items = array.items(arrayObject.getScope());
		final ArrayItem[] defItems = new ArrayItem[items.length];

		for (int i = 0; i < items.length; ++i) {

			final Ref valueRef = items[i].getValueRef();
			final Ref defValueRef = valueRef.prefixWith(prefix);

			defItems[i] = new ArrayItem(i, defValueRef);
		}

		return resultStruct.compilerValue(
				new Array(
						array,
						array.distributeIn(resolver.getContainer()),
						resultStruct,
						defItems));
	}

	private static final class ArrayEval implements Eval {

		private final Ref ref;

		ArrayEval(Ref ref) {
			this.ref = ref;
		}

		public final Ref getRef() {
			return this.ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValueStruct<?, ?> fromValueStruct =
					getRef().valueStruct(getRef().getScope());
			final ValDirs fromDirs = dirs.dirs().nested().value(
					fromValueStruct,
					TEMP_VAL_HOLDER);
			final ValOp from = getRef().op(host).writeValue(fromDirs);
			final FuncPtr<ValCopyFunc> func =
					dirs.getGenerator()
					.externalFunction()
					.link("o42a_array_copy", VAL_COPY);

			func.op(null, dirs.code()).copy(dirs, from);
			fromDirs.done();
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}

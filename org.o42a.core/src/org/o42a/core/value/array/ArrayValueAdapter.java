/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.value.array;

import static org.o42a.core.ir.value.ValCopyFn.VAL_COPY;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValCopyFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.link.TargetResolver;


final class ArrayValueAdapter extends ValueAdapter {

	private final TypeParameters<Array> expectedParameters;

	ArrayValueAdapter(
			Ref adaptedRef,
			TypeParameters<Array> expectedParameters) {
		super(adaptedRef);
		this.expectedParameters = expectedParameters;
	}

	public final TypeParameters<Array> getExpectedParameters() {
		return this.expectedParameters;
	}

	@Override
	public boolean isConstant() {
		if (getExpectedParameters().getValueType().isVariable()) {
			return false;
		}
		return getAdaptedRef().isConstant();
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return value(scope.resolver()).getTypeParameters();
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return arrayValue(
				getAdaptedRef(),
				resolver,
				getExpectedParameters());
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
		if (getExpectedParameters().getValueType().isVariable()) {
			return false;
		}
		return !getAdaptedRef().getValueType().isVariable();
	}

	private static Value<?> arrayValue(
			Ref ref,
			Resolver resolver,
			TypeParameters<Array> expectedParameters) {

		final Resolution arrayResolution = ref.resolve(resolver);

		if (arrayResolution.isError()) {
			return expectedParameters.falseValue();
		}

		final Obj arrayObject = arrayResolution.toObject();
		final Value<?> value = arrayObject.value().getValue();
		final TypeParameters<Array> sourceParams =
				value.getTypeParameters().toArrayParameters();
		final ArrayValueType sourceType =
				sourceParams.getValueType().toArrayType();

		final PrefixPath prefix = ref.getPath().toPrefix(resolver.getScope());
		final TypeParameters<Array> resultParams =
				sourceParams.convertTo(sourceType.setVariable(
						expectedParameters.getValueType().isVariable()))
				.prefixWith(prefix);

		if (value.getKnowledge().isFalse()) {
			return resultParams.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultParams.runtimeValue();
		}
		if (sourceType.isVariable()) {
			// Mutable array can not be copied at compile time.
			return resultParams.runtimeValue();
		}

		final Array array = sourceParams.cast(value).getCompilerValue();
		final ArrayItem[] items = array.items(arrayObject.getScope());
		final ArrayItem[] defItems = new ArrayItem[items.length];

		for (int i = 0; i < items.length; ++i) {

			final Ref valueRef = items[i].getValueRef();
			final Ref defValueRef = valueRef.prefixWith(prefix);

			defItems[i] = new ArrayItem(i, defValueRef);
		}

		return resultParams.compilerValue(
				new Array(
						array,
						array.distributeIn(resolver.getContainer()),
						resultParams,
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

			final ValDirs fromDirs = dirs.dirs().nested().value(
					getRef().getValueType(),
					TEMP_VAL_HOLDER);
			final Block code = fromDirs.code();
			final ValOp from = getRef().op(host).writeValue(fromDirs);
			final FuncPtr<ValCopyFn> func =
					dirs.getGenerator()
					.externalFunction()
					.link("o42a_array_copy", VAL_COPY);

			func.op(null, code).copy(fromDirs.dirs(), from, dirs.value());

			fromDirs.done().code().go(dirs.returnDir());
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

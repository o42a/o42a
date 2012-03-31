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
package org.o42a.core.object.array.impl;

import static org.o42a.core.ir.value.ValCopyFunc.VAL_COPY;
import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValCopyFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.Array;
import org.o42a.core.object.array.ArrayItem;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


final class ArrayCopyValueDef extends ValueDef {

	static Value<?> arrayValue(Ref ref, Resolver resolver, boolean toConstant) {

		final Resolution arrayResolution = ref.resolve(resolver);

		if (arrayResolution.isError()) {
			return falseValue();
		}

		final Obj arrayObject = arrayResolution.toObject();
		final Value<?> value =
				arrayObject.value().explicitUseBy(resolver).getValue();
		final ArrayValueStruct sourceStruct =
				(ArrayValueStruct) value.getValueStruct();

		final PrefixPath prefix = ref.getPath().toPrefix(resolver.getScope());
		final ArrayValueStruct resultStruct =
				sourceStruct.setConstant(toConstant).prefixWith(prefix);

		if (value.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultStruct.runtimeValue();
		}
		if (!sourceStruct.isConstant()) {
			// Non-constant array can not be copied at compile time.
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

	private final Ref ref;
	private final boolean toConstant;
	private ArrayValueStruct fromStruct;
	private ArrayValueStruct toStruct;

	ArrayCopyValueDef(Ref ref, boolean toConstant) {
		super(sourceOf(ref), ref, ScopeUpgrade.noScopeUpgrade(ref.getScope()));
		this.ref = ref;
		this.toConstant = toConstant;
	}

	private ArrayCopyValueDef(
			ArrayCopyValueDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
		this.toConstant = prototype.toConstant;
	}

	@Override
	public ArrayValueStruct getValueStruct() {
		if (this.toStruct != null) {
			return this.toStruct;
		}
		return this.toStruct = fromValueStruct().setConstant(this.toConstant);
	}

	@Override
	public void normalize(Normalizer normalizer) {
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return arrayValue(this.ref, resolver, this.toConstant);
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ArrayCopyValueDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolve(resolver).resolveValue();
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		if (fromConstToConst()) {
			// Constant array can be copied by reference.
			return this.ref.op(host).writeValue(dirs);
		}

		final ValDirs fromDirs = dirs.dirs().value(fromValueStruct());
		final ValOp from = this.ref.op(host).writeValue(fromDirs);

		final FuncPtr<ValCopyFunc> func =
				dirs.getGenerator()
				.externalFunction()
				.link("o42a_array_copy", VAL_COPY);

		func.op(null, dirs.code()).copy(dirs, from);
		fromDirs.done();

		return dirs.value();
	}

	private final boolean fromConstToConst() {
		return fromValueStruct().isConstant() && getValueStruct().isConstant();
	}

	private final ArrayValueStruct fromValueStruct() {
		if (this.fromStruct != null) {
			return this.fromStruct;
		}

		final Scope scope = getScopeUpgrade().rescope(getScope());

		return this.fromStruct =
				(ArrayValueStruct) this.ref.valueStruct(scope).prefixWith(
						getScopeUpgrade().toPrefix());
	}

}

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
import static org.o42a.core.object.array.impl.ArrayCopyDef.arrayValue;

import org.o42a.codegen.code.FuncPtr;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValCopyFunc;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.array.ArrayValueStruct;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.value.*;


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
		if (getExpectedStruct().getValueType().isRuntimeConstructed()) {
			return false;
		}
		return getAdaptedRef().isConstant();
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public Def valueDef() {
		return new ArrayCopyDef(
				getAdaptedRef(),
				getExpectedStruct().isVariable());
	}

	@Override
	public Logical logical(Scope scope) {
		return getAdaptedRef().rescope(scope).getLogical();
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
	public LogicalValue initialCond(LocalResolver resolver) {
		return getAdaptedRef().value(resolver).getKnowledge().toLogicalValue();
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public RefEval eval(CodeBuilder builder) {
		if (fromConstToConst()) {
			return new RefOpEval(builder, getAdaptedRef());
		}
		return new ArrayEval(builder, getAdaptedRef());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getAdaptedRef().resolve(resolver).resolveValue();
	}

	private boolean fromConstToConst() {
		if (getExpectedStruct().isVariable()) {
			return false;
		}
		return !getAdaptedRef().getValueType().isVariable();
	}

	private static final class ArrayEval extends RefEval {

		ArrayEval(CodeBuilder builder, Ref ref) {
			super(builder, ref);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			getRef().op(host).writeCond(dirs);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValueStruct<?, ?> fromValueStruct =
					getRef().valueStruct(getRef().getScope());
			final ValDirs fromDirs = dirs.dirs().value(fromValueStruct);
			final ValOp from = getRef().op(host).writeValue(fromDirs);
			final FuncPtr<ValCopyFunc> func =
					dirs.getGenerator()
					.externalFunction()
					.link("o42a_array_copy", VAL_COPY);

			func.op(null, dirs.code()).copy(dirs, from);
			fromDirs.done();
		}

	}

}

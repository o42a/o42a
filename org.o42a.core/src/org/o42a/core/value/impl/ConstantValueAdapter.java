/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.*;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;


final class ConstantValueAdapter<T> extends ValueAdapter {

	private final SingleValueType<T> valueType;
	private final Value<T> value;

	ConstantValueAdapter(
			Ref adaptedRef,
			SingleValueType<T> valueType,
			T constant) {
		super(adaptedRef);
		this.valueType = valueType;
		this.value =
				typeParameters(adaptedRef.getScope())
				.compilerValue(constant);
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public TypeParameters<T> typeParameters(Scope scope) {
		return TypeParameters.typeParameters(getAdaptedRef(), this.valueType);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return this.value;
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return new InlineConstant(this.value);
	}

	@Override
	public Eval eval() {
		return new ConstantEval(this.value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "null";
		}
		return this.value.toString();
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

	private static final class ConstantEval implements Eval {

		private final Value<?> value;

		ConstantEval(Value<?> value) {
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.value.op(dirs.getBuilder(), dirs.code()));
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

	private static final class InlineConstant extends InlineValue {

		private final Value<?> value;

		InlineConstant(Value<?> value) {
			super(null);
			this.value = value;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.value.op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

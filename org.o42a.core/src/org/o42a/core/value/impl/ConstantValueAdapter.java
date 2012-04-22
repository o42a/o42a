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
package org.o42a.core.value.impl;

import static org.o42a.core.object.def.Def.sourceOf;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.value.*;
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
		this.value = valueType.constantValue(constant);
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
	public Def valueDef() {
		return this.valueType.struct().constantDef(
				sourceOf(getAdaptedRef()),
				getAdaptedRef(),
				this.value.getCompilerValue());
	}

	@Override
	public Logical logical(Scope scope) {
		return logicalTrue(getAdaptedRef(), scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return this.valueType.constantValue(this.value.getCompilerValue());
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
		return LogicalValue.TRUE;
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return new InlineConstant(this.valueType.struct(), this.value);
	}

	@Override
	public RefEval eval(CodeBuilder builder) {
		return new ConstantEval(builder, getAdaptedRef(), this.value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "null";
		}
		return this.value.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
	}

	private static final class ConstantEval extends RefEval {

		private final Value<?> value;

		ConstantEval(CodeBuilder builder, Ref ref, Value<?> value) {
			super(builder, ref);
			this.value = value;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			// Always TRUE.
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

		InlineConstant(ValueStruct<?, ?> valueStruct, Value<?> value) {
			super(null, valueStruct);
			this.value = value;
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

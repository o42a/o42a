/*
    Compiler Core
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
package org.o42a.core.value.macro.impl;


import static org.o42a.core.value.macro.impl.EmptyMacro.EMPTY_MACRO;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;


public class MacroValueAdapter extends ValueAdapter {

	private static final InlineMacroValue INLINE_MACRO_VALUE =
			new InlineMacroValue();

	public MacroValueAdapter(Ref adaptedRef) {
		super(adaptedRef);
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
	public Value<?> value(Resolver resolver) {

		final Ref ref = getAdaptedRef().upgradeScope(resolver.getScope());

		return ValueType.MACRO.compilerValue(new MacroRef(ref));
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return INLINE_MACRO_VALUE;
	}

	@Override
	public Eval eval() {
		return Eval.MACRO_EVAL;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

	private static final class InlineMacroValue extends InlineValue {

		InlineMacroValue() {
			super(null);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return ValueType.MACRO
					.constantValue(EMPTY_MACRO)
					.op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public String toString() {
			return "MACRO";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

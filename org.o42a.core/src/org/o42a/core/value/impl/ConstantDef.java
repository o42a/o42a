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
package org.o42a.core.value.impl;

import static org.o42a.core.object.meta.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.util.fn.Cancelable;


final class ConstantDef<T> extends Def {

	private final Value<T> value;

	ConstantDef(ConstantObject<T> source) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.value = source.getValue();
	}

	private ConstantDef(
			ConstantDef<T> prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.value = prototype.value;
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public EscapeMode getEscapeMode() {
		return ESCAPE_IMPOSSIBLE;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return new ConstantEval(this.value);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		// No need to normalize the scalar constant.
	}

	@Override
	public Eval eval() {
		return new ConstantEval(this.value);
	}

	@Override
	protected boolean hasConstantValue() {
		return true;
	}

	@Override
	protected TypeParameters<?> typeParameters(Scope scope) {
		return this.value.getTypeParameters();
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(this.value);
	}

	@Override
	protected ConstantDef<T> create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ConstantDef<>(this, upgrade);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.value.resolveAll(resolver);
	}

	private static final class ConstantEval extends InlineEval {

		private final Value<?> value;

		public ConstantEval(Value<?> value) {
			super(null);
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

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

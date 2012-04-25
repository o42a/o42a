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
package org.o42a.core.object.def;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


final class VoidDef extends Def {

	private final Def def;

	VoidDef(Def def) {
		super(
				def.getSource(),
				def.getLocation(),
				def.getScopeUpgrade());
		this.def = def;
	}

	private VoidDef(
			VoidDef prototype,
			ScopeUpgrade scopeUpgrade,
			ScopeUpgrade additionalUpgrade) {
		super(prototype, scopeUpgrade);
		this.def = prototype.def.upgradeScope(additionalUpgrade);
	}

	@Override
	public final ValueStruct<?, ?> getValueStruct() {
		return ValueStruct.VOID;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public Value<?> value(Resolver resolver) {

		final Value<?> value = this.def.value(resolver);

		return value.getKnowledge().getCondition().toValue(ValueStruct.VOID);
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {

		final InlineEval inline = this.def.inline(normalizer);

		if (inline == null) {
			return null;
		}

		return new EvalToVoid(this.def.getValueStruct(), inline);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.def.normalize(normalizer);
	}

	@Override
	public Eval eval() {
		return new EvalToVoid(this.def.getValueStruct(), this.def.eval());
	}

	@Override
	protected boolean hasConstantValue() {
		return this.def.hasConstantValue();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Def create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new VoidDef(this, upgrade, additionalUpgrade);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.def.fullyResolve(resolver);
	}

	private static final class EvalToVoid extends InlineEval {

		private final ValueStruct<?, ?> valueStruct;
		private final Eval def;

		EvalToVoid(ValueStruct<?, ?> valueStruct, Eval def) {
			super(null);
			this.valueStruct = valueStruct;
			this.def = def;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Block trueVal = dirs.addBlock("true_def");
			final DefDirs defDirs =
					dirs.dirs().value(this.valueStruct).def(trueVal.head());

			this.def.write(defDirs, host);
			defDirs.done();

			if (trueVal.exists()) {
				trueVal.go(dirs.code().tail());
			}
		}

		@Override
		public String toString() {
			if (this.def == null) {
				return super.toString();
			}
			return "(void) "+ this.def.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

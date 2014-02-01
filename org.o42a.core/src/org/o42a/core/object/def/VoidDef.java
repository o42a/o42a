/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
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
	public boolean unconditional() {
		return true;
	}

	@Override
	public DefValue value(Resolver resolver) {

		final DefValue value = this.def.value(resolver);

		if (!value.hasValue()) {
			return value;
		}

		return defValue(
				value.getValue()
				.getKnowledge()
				.getCondition()
				.toValue(typeParameters(resolver.getScope())));
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {

		final InlineEval inline = this.def.inline(normalizer);

		if (inline == null) {
			return null;
		}

		return new EvalToVoid(this.def.getValueType(), inline);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.def.normalize(normalizer);
	}

	@Override
	public Eval eval() {
		return new EvalToVoid(this.def.getValueType(), this.def.eval());
	}

	@Override
	protected boolean hasConstantValue() {
		return this.def.hasConstantValue();
	}

	@Override
	protected TypeParameters<Void> typeParameters(Scope scope) {
		return TypeParameters.typeParameters(this, ValueType.VOID);
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Def create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new VoidDef(this, upgrade, additionalUpgrade);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.def.fullyResolve(resolver);
	}

	private static final class EvalToVoid extends InlineEval {

		private final ValueType<?> valueType;
		private final Eval def;

		EvalToVoid(ValueType<?> valueType, Eval def) {
			super(null);
			this.valueType = valueType;
			this.def = def;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Block trueVal = dirs.addBlock("true_def");
			final DefDirs defDirs =
					dirs.dirs()
					.nested()
					.value(this.valueType, TEMP_VAL_HOLDER)
					.def(trueVal.head());

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

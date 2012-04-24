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

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
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
	public void normalize(RootNormalizer normalizer) {
		this.def.normalize(normalizer);
	}

	@Override
	protected ValOp writeDef(ValDirs dirs, HostOp host) {

		final ValueStruct<?, ?> valueStruct = this.def.getValueStruct();
		final ValDirs defDirs = dirs.dirs().value(valueStruct);

		this.def.write(defDirs, host);
		defDirs.done();

		return Value.voidValue().op(dirs.getBuilder(), dirs.code());
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

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineValue inline =
				this.def.inline(normalizer, this.def.getValueStruct());

		if (inline == null) {
			return null;
		}

		return new Inline(inline);
	}

	private static final class Inline extends InlineValue {

		private final InlineValue inline;

		Inline(InlineValue inline) {
			super(null, ValueStruct.VOID);
			this.inline = inline;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.inline.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final ValueStruct<?, ?> valueStruct = this.inline.getValueStruct();
			final ValDirs defDirs = dirs.dirs().value(valueStruct);

			this.inline.writeValue(defDirs, host);
			defDirs.done();

			return Value.voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public String toString() {
			if (this.inline == null) {
				return super.toString();
			}
			return "(void) "+ this.inline.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

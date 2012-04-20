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

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public final class ConstantDef<T> extends Def {

	private final Value<T> value;

	public ConstantDef(Obj source, LocationInfo location, Value<T> value) {
		super(source, location, noScopeUpgrade(source.getScope()));
		this.value = value;
	}

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
	public ValueStruct<?, ?> getValueStruct() {
		return this.value.getValueStruct();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		// No need to normalize the scalar constant.
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	protected boolean hasConstantValue() {
		return true;
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.value;
	}

	@Override
	protected ConstantDef<T> create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ConstantDef<T>(this, upgrade);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.value.resolveAll(resolver);
	}

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return new Inline(this.value);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.value.op(dirs.getBuilder(), dirs.code());
	}

	private static final class Inline extends InlineValue {

		private final Value<?> value;

		public Inline(Value<?> value) {
			super(null, value.getValueStruct());
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

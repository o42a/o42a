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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class ConstantValueDef<T> extends ValueDef {

	private final Value<T> value;

	public ConstantValueDef(Obj source, LocationInfo location, Value<T> value) {
		super(source, location, noScopeUpgrade(source.getScope()));
		this.value = value;
	}

	ConstantValueDef(ConstantObject<T> source) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.value = source.getValue();
	}

	private ConstantValueDef(
			ConstantValueDef<T> prototype,
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
	protected boolean hasConstantValue() {
		return true;
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.value;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected Logical buildLogical() {
		return logicalTrue(this, getSource().getScope());
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ConstantValueDef<T>(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.value.resolveAll(resolver);
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return new Inline(this.value);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.value.op(dirs.getBuilder(), dirs.code());
	}

	@Override
	protected String name() {
		return "ConstantValueDef";
	}

	private static final class Inline extends InlineValue {

		private final Value<?> value;

		public Inline(Value<?> value) {
			super(value.getValueStruct());
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
		public void cancel() {
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

}

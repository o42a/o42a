/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.def.impl;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.Scope;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class RefValueDef extends ValueDef {

	private final Common common;

	public RefValueDef(Ref ref) {
		super(sourceOf(ref), ref, noScopeUpgrade(ref.getScope()));
		this.common = new Common(ref);
	}

	private RefValueDef(RefValueDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.common = prototype.common;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final Scope scope = getScopeUpgrade().rescope(getScope());

		return this.common.ref.valueStruct(scope).prefixWith(
				getScopeUpgrade().toPrefix());
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.common.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.common.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.common.ref.getLogical();
	}

	@Override
	protected boolean hasConstantValue() {
		return this.common.ref.isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.common.ref.value(resolver);
	}

	@Override
	protected RefValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RefValueDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.common.ref.resolve(resolver).resolveValue();
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return this.common.ref.inline(normalizer, getScope());
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		this.common.inline = inline(normalizer, getValueStruct());
	}

	@Override
	protected ValOp writeDef(ValDirs dirs, HostOp host) {

		final InlineValue inline = this.common.inline;

		if (inline != null) {
			return inline.writeValue(dirs, host);
		}

		return super.writeDef(dirs, host);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.common.ref.op(host).writeValue(dirs);
	}

	private static final class Common {

		private final Ref ref;
		private InlineValue inline;

		Common(Ref ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return "null";
			}
			return this.ref.toString();
		}

	}

}

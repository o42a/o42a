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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class RefDef extends Def {

	private final Ref ref;
	private InlineValue inline;

	public RefDef(Ref ref) {
		super(sourceOf(ref), ref, noScopeUpgrade(ref.getScope()));
		this.ref = ref;
	}

	private RefDef(RefDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final Scope scope = getScopeUpgrade().rescope(getScope());

		return this.ref.valueStruct(scope).prefixWith(
				getScopeUpgrade().toPrefix());
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.ref.normalize(normalizer.getAnalyzer());
		this.inline = inline(normalizer.newNormalizer(), getValueStruct());
	}

	@Override
	protected boolean hasConstantValue() {
		return this.ref.isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.ref.value(resolver);
	}

	@Override
	protected RefDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RefDef(this, upgrade);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.ref.resolve(resolver).resolveValue();
	}

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return this.ref.inline(normalizer, getScope());
	}

	@Override
	protected ValOp writeDef(ValDirs dirs, HostOp host) {

		final InlineValue inline = this.inline;

		if (inline != null) {
			return inline.writeValue(dirs, host);
		}

		return this.ref.op(host).writeValue(dirs);
	}

}

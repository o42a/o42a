/*
    Modules Commons
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
package org.o42a.common.def;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class BuiltinDef extends Def {

	private final Builtin builtin;
	private InlineEval normal;

	public BuiltinDef(Builtin builtin) {
		super(
				builtin.toObject(),
				builtin,
				noScopeUpgrade(builtin.toObject().getScope()));
		this.builtin = builtin;
	}

	private BuiltinDef(BuiltinDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.builtin = prototype.builtin;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.builtin.toObject().value().getValueStruct();
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return this.builtin.inlineBuiltin(normalizer, getScope());
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.normal = inline(normalizer.newNormalizer());
	}

	@Override
	public Eval eval() {
		if (this.normal != null) {
			return this.normal;
		}
		return this.builtin.evalBuiltin();
	}

	@Override
	protected boolean hasConstantValue() {
		return this.builtin.isConstantBuiltin();
	}

	@Override
	protected BuiltinDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new BuiltinDef(this, upgrade);
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.builtin.calculateBuiltin(resolver);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {

		final Obj object = resolver.getContainer().toObject();
		final Obj builtin = this.builtin.toObject();

		if (builtin != object) {
			builtin.value().resolveAll(resolver);
		}
		object.resolveAll();
		this.builtin.resolveBuiltin(
				object.value().part(isClaim()).resolver());
	}

}

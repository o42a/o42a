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
package org.o42a.core.object.def.impl;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.RUNTIME_DEF_VALUE;

import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;


public final class RuntimeDef extends Def {

	private final Definitions definitions;

	public RuntimeDef(Definitions definitions, boolean claim) {
		super(
				/* The source should differ from scope,
				 * as this definition is not explicit. */
				definitions.getContext().getVoid(),
				definitions,
				noScopeUpgrade(definitions.getScope()),
				claim);
		this.definitions = definitions;
	}

	private RuntimeDef(RuntimeDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.definitions = prototype.definitions;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
	}

	@Override
	protected RuntimeDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RuntimeDef(this, upgrade);
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.definitions.getValueStruct();
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Eval eval() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return RUNTIME_DEF_VALUE;
	}

}

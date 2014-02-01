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
package org.o42a.core.value.macro;

import static org.o42a.core.ir.def.InlineEval.macroInlineEval;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public final class MacroDef extends Def {

	private final Macro macro;

	public MacroDef(Obj source, LocationInfo location, Macro macro) {
		super(source, location, noScopeUpgrade(source.getScope()));
		this.macro = macro;
	}

	private MacroDef(MacroDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.macro = prototype.macro;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return macroInlineEval();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	public Eval eval() {
		return Eval.MACRO_EVAL;
	}

	@Override
	protected Def create(ScopeUpgrade upgrade, ScopeUpgrade additionalUpgrade) {
		return new MacroDef(this, upgrade);
	}

	@Override
	protected boolean hasConstantValue() {
		return true;
	}

	@Override
	protected TypeParameters<Macro> typeParameters(Scope scope) {
		return TypeParameters.typeParameters(this, ValueType.MACRO);
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {

		final Value<Macro> value =
				typeParameters(resolver.getScope())
				.compilerValue(this.macro);

		return defValue(value);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

}

/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import static org.o42a.core.ir.def.Eval.VOID_EVAL;
import static org.o42a.core.ir.def.InlineEval.voidInlineEval;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;
import static org.o42a.core.value.Void.VOID;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;


final class VoidDef extends Def {

	VoidDef(Definitions definitions) {
		super(
				definitions.getScope().toObject(),
				definitions.getLocation(),
				noScopeUpgrade(definitions.getScope()));
	}

	private VoidDef(VoidDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public EscapeMode getEscapeMode() {
		return ESCAPE_IMPOSSIBLE;
	}

	@Override
	public DefValue value(Resolver resolver) {
		return defValue(typeParameters(resolver.getScope())
				.compilerValue(VOID));
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return voidInlineEval();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	public Eval eval() {
		return VOID_EVAL;
	}

	@Override
	public String toString() {
		return "=void";
	}

	@Override
	protected boolean hasConstantValue() {
		return true;
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
		return new VoidDef(this, upgrade);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

}

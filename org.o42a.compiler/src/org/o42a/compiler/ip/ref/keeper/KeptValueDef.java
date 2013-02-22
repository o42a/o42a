/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.keeper;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;


final class KeptValueDef extends Def {

	private final Keeper keeper;

	KeptValueDef(Obj source, Ref value) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.keeper = source.keepers().keep(
				this,
				value.rescope(source.getScope()));
	}

	private KeptValueDef(
			KeptValueDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.keeper = prototype.getKeeper();
	}

	public final Ref getValue() {
		return this.keeper.getValue();
	}

	public final Keeper getKeeper() {
		return this.keeper;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		getValue().upgradeScope(normalizer.getNormalizedScope())
		.normalize(normalizer.getAnalyzer());
	}

	@Override
	public Eval eval() {
		return new KeptValueEval(getKeeper());
	}

	@Override
	public String toString() {
		if (this.keeper == null) {
			return null;
		}
		return "= //" + getValue();
	}

	@Override
	protected Def create(ScopeUpgrade upgrade, ScopeUpgrade additionalUpgrade) {
		return new KeptValueDef(this, upgrade);
	}

	@Override
	protected boolean hasConstantValue() {
		return getValue().isConstant();
	}

	@Override
	protected TypeParameters<?> typeParameters(Scope scope) {
		return getValue().typeParameters(scope);
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(getValue().value(resolver));
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValue().resolveAll(resolver);

		final Obj object = resolver.getScope().toObject();

		object.type().useBy(resolver.refUser());
	}

}

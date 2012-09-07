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
package org.o42a.core.object.state.impl;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.ValueStruct;


final class KeeperDef extends Def {

	private final KeeperObject keeperObject;

	KeeperDef(KeeperObject source) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.keeperObject = source;
	}

	private KeeperDef(KeeperDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.keeperObject = prototype.keeperObject;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.keeperObject.value()
				.getValueStruct()
				.upgradeScope(getScope());
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Eval eval() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		if (this.keeperObject == null) {
			return super.toString();
		}
		return "=" + this.keeperObject.getKeeper();
	}

	@Override
	protected Def create(ScopeUpgrade upgrade, ScopeUpgrade additionalUpgrade) {
		return new KeeperDef(this, upgrade);
	}

	@Override
	protected boolean hasConstantValue() {
		return getValue().isConstant();
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(getValue().value(resolver));
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValue().resolveAll(resolver);
	}

	private Ref getValue() {
		return this.keeperObject.getValue();
	}

}

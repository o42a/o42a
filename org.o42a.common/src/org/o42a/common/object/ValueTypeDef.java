/*
    Modules Commons
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
package org.o42a.common.object;

import static org.o42a.core.ir.def.InlineEval.noInlineEval;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.FALSE_DEF_VALUE;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;


final class ValueTypeDef extends Def {

	private final ValueType<?> valueType;

	ValueTypeDef(ValueTypeObject object) {
		super(object, object.getScope(), noScopeUpgrade(object.getScope()));
		this.valueType = object.type().getValueType();
	}

	public ValueTypeDef(
			ValueTypeDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.valueType = prototype.valueType;
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		return noInlineEval();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	public Eval eval() {
		return noInlineEval();
	}

	@Override
	public String toString() {
		if (this.valueType == null) {
			return super.toString();
		}
		return this.valueType.toString();
	}

	@Override
	protected Def create(ScopeUpgrade upgrade, ScopeUpgrade additionalUpgrade) {
		return new ValueTypeDef(this, upgrade);
	}

	@Override
	protected boolean hasConstantValue() {
		return true;
	}

	@Override
	protected TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return FALSE_DEF_VALUE;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

}

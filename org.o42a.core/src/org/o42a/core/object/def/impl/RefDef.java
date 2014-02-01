/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;


public final class RefDef extends Def {

	private final Ref ref;
	private InlineEval normal;

	public RefDef(Ref ref) {
		super(sourceOf(ref), ref, noScopeUpgrade(ref.getScope()));
		this.ref = ref;
	}

	private RefDef(RefDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {

		final InlineValue inline = this.ref.inline(normalizer, getScope());

		if (inline == null) {
			return null;
		}

		return inline.toInlineEval();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.ref.normalize(normalizer.getAnalyzer());
		this.normal = inline(normalizer.newNormalizer());
	}

	@Override
	public Eval eval() {
		if (this.normal != null) {
			return this.normal;
		}
		return new RefOpEval(this.ref);
	}

	@Override
	protected boolean hasConstantValue() {
		return this.ref.isConstant();
	}

	@Override
	protected TypeParameters<?> typeParameters(Scope scope) {
		return this.ref.typeParameters(scope);
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(this.ref.value(resolver));
	}

	@Override
	protected RefDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new RefDef(this, upgrade);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.ref.resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

}

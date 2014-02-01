/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ref.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.link.TargetResolver;


public class RawValueAdapter extends ValueAdapter {

	public RawValueAdapter(Ref adaptedRef) {
		super(adaptedRef);
	}

	@Override
	public boolean isConstant() {
		return getAdaptedRef().isConstant();
	}

	@Override
	public Ref toTarget() {
		return null;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getAdaptedRef().typeParameters(scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return getAdaptedRef().value(resolver);
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline = getAdaptedRef().inline(normalizer, origin);

		return inline != null ? inline : null;
	}

	@Override
	public Eval eval() {
		return new RefOpEval(getAdaptedRef());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

}

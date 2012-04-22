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
package org.o42a.core.value.impl;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.RefEval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.impl.RefDef;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;


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
	public Def valueDef() {
		return new RefDef(getAdaptedRef());
	}

	@Override
	public Logical logical(Scope scope) {
		return getAdaptedRef().rescope(scope).getLogical();
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return getAdaptedRef().value(resolver);
	}

	@Override
	public LogicalValue initialCond(LocalResolver resolver) {
		return getAdaptedRef().value(resolver).getKnowledge().toLogicalValue();
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
	public RefEval eval(CodeBuilder builder) {
		return new RefOpEval(builder, getAdaptedRef());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getAdaptedRef().resolve(resolver).resolveValue();
	}

}

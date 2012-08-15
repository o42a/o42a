/*
    Compiler
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
package org.o42a.compiler.ip.st.macro;

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;


final class ExpandMacroDefiner extends Definer {

	private final IdentityHashMap<Scope, ValueAdapter> adapters =
			new IdentityHashMap<Scope, ValueAdapter>(1);

	ExpandMacroDefiner(ExpandMacroStatement statement, DefinerEnv env) {
		super(statement, env);
	}

	public final Ref getExpansion() {

		final ExpandMacroStatement statement =
				(ExpandMacroStatement) getStatement();

		return statement.getExpansion();
	}

	@Override
	public DefTargets getDefTargets() {
		return valueDef();
	}

	@Override
	public DefValue value(Resolver resolver) {
		return valueAdapter(resolver.getScope()).value(resolver).toDefValue();
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		valueAdapter(origin).resolveTargets(resolver);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline =
				valueAdapter(origin).inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return inline.toInlineEval();
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		assert getStatement().assertFullyResolved();
		return valueAdapter(origin).eval();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final Ref target = valueAdapter(origin).toTarget();

		if (target == null) {
			return DefTarget.NO_DEF_TARGET;
		}

		return new DefTarget(target);
	}

	public ValueAdapter valueAdapter(Scope scope) {

		final ValueAdapter cached = this.adapters.get(scope);

		if (cached != null) {
			return cached;
		}

		final ValueStruct<?, ?> expectedStruct =
				env().getExpectedValueStruct().upgradeScope(scope);

		final ValueAdapter adapter =
				getExpansion()
				.rebuildIn(scope)
				.valueAdapter(expectedStruct, true);

		this.adapters.put(scope, adapter);

		return adapter;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		valueAdapter(resolver.getScope()).resolveAll(resolver);
	}

}
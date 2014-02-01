/*
    Modules Commons
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.common.macro.st;

import static org.o42a.core.value.macro.MacroConsumer.DEFAULT_MACRO_EXPANSION_LOGGER;

import java.util.IdentityHashMap;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.EvalCmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.link.TargetResolver;


final class ExpandMacroCommand extends Command {

	private final IdentityHashMap<Scope, ValueAdapter> adapters =
			new IdentityHashMap<>(1);

	ExpandMacroCommand(ExpandMacroStatement statement, CommandEnv env) {
		super(statement, env);
	}

	public final Ref getExpansion() {

		final ExpandMacroStatement statement =
				(ExpandMacroStatement) getStatement();

		return statement.getExpansion();
	}

	@Override
	public CommandTargets getTargets() {
		return returnCommand();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {

		final ValueAdapter valueAdapter = valueAdapter(scope);

		if (!valueAdapter.getAdaptedRef().isValid()) {
			return null;
		}

		return valueAdapter.typeParameters(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return new ReturnValue(
				this,
				valueAdapter(resolver.getScope()).value(resolver));
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		valueAdapter(origin).resolveTargets(resolver);
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline =
				valueAdapter(origin).inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return inline.toInlineCmd();
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return inline(normalizer.newNormalizer(), origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return new EvalCmd(valueAdapter(origin).eval());
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

		final TypeParameters<?> expectedParameters =
				env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);
		final CompilerLogger logger =
				DEFAULT_MACRO_EXPANSION_LOGGER.compilerLogger(
						scope,
						getContext().getLogger());
		final ValueRequest valueRequest =
				new ValueRequest(expectedParameters, logger);
		final ValueAdapter adapter =
				getExpansion().rebuildIn(scope).valueAdapter(valueRequest);

		this.adapters.put(scope, adapter);

		return adapter;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		valueAdapter(resolver.getScope()).resolveAll(resolver);
	}

}

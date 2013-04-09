/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ref.impl;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.EvalCmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.impl.RefDef;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.link.TargetResolver;


public final class RefCommand extends Command {

	private ValueAdapter valueAdapter;

	public RefCommand(Ref ref, CommandEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	@Override
	public final CommandTargets getTargets() {
		if (!getRef().isConstant()) {
			return returnCommand();
		}
		return returnCommand().setConstant();
	}

	public ValueAdapter getValueAdapter() {
		if (this.valueAdapter != null) {
			return this.valueAdapter;
		}

		final ValueRequest valueRequest = env().getValueRequest();

		return this.valueAdapter = getRef().valueAdapter(valueRequest);
	}

	public Definitions createDefinitions() {

		final RefDef def = new RefDef(getRef());

		return def.toDefinitions(
				env().getValueRequest().getExpectedParameters());
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getValueAdapter().typeParameters(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return new ReturnValue(
				this,
				getValueAdapter().value(resolver));
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline = getValueAdapter().inline(normalizer, origin);

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
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		getValueAdapter().resolveTargets(resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final Ref target = getValueAdapter().toTarget();

		if (target == null) {
			return DefTarget.NO_DEF_TARGET;
		}

		return new DefTarget(target);
	}

	@Override
	public final Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return new EvalCmd(getValueAdapter().eval());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValueAdapter().resolveAll(resolver);
	}

}

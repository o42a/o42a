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
package org.o42a.core.ref.impl;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Instruction;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;
import org.o42a.core.value.link.TargetResolver;


abstract class RefCommand extends Command {

	private ValueAdapter valueAdapter;

	RefCommand(Ref ref, CommandEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	public final ValueAdapter getValueAdapter() {
		if (this.valueAdapter != null) {
			return this.valueAdapter;
		}

		final ValueRequest valueRequest = env().getValueRequest();

		return this.valueAdapter = getRef().valueAdapter(valueRequest);
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getValueAdapter().typeParameters(scope);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		getValueAdapter().resolveTargets(resolver);
	}

	@Override
	public final Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return refCmd(getValueAdapter().eval());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getValueAdapter().resolveAll(resolver);
	}

	protected abstract Cmd refCmd(Eval value);

}

/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.member;

import static org.o42a.core.ir.def.InlineEval.noInlineEval;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


public abstract class DeclarationDefiner extends Definer {

	public DeclarationDefiner(
			DeclarationStatement statement,
			CommandEnv env) {
		super(statement, env);
	}

	public final DeclarationStatement getDeclarationStatement() {
		return (DeclarationStatement) getStatement();
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public final DefTarget toTarget(Scope origin) {
		return null;
	}

	@Override
	public final TypeParameters<?> typeParameters(Scope scope) {
		return null;
	}

	@Override
	public final DefValue value(Resolver resolver) {
		return TRUE_DEF_VALUE;
	}

	@Override
	public final void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public final InlineEval inline(Normalizer normalizer, Scope origin) {
		return noInlineEval();
	}

	@Override
	public final InlineEval normalize(
			RootNormalizer normalizer,
			Scope origin) {
		return null;
	}

	@Override
	public final Eval eval(CodeBuilder builder, Scope origin) {
		return null;
	}

	@Override
	protected final void fullyResolve(FullResolver resolver) {
	}

}

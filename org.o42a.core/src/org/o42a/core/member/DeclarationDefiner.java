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
package org.o42a.core.member;

import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;


public abstract class DeclarationDefiner extends Definer {

	public DeclarationDefiner(
			DeclarationStatement statement,
			DefinerEnv env) {
		super(statement, env);
	}

	public final DeclarationStatement getDeclarationStatement() {
		return (DeclarationStatement) getStatement();
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefTarget toTarget() {
		return null;
	}

	@Override
	public DefValue value(Resolver resolver) {
		return TRUE_DEF_VALUE;
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return InlineEval.noInlineEval();
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval(CodeBuilder builder) {
		return null;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
	}

}

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

import static org.o42a.core.ref.InlineValue.inlineUnknown;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueStruct;


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
	public final Definitions define(Scope scope) {
		return null;
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public DefValue value(Resolver resolver) {
		return TRUE_DEF_VALUE;
	}

	@Override
	public InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return inlineUnknown(valueStruct);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		return null;
	}

}

/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Instruction;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueStruct;


public abstract class DeclarationDefiner extends Definer {

	public DeclarationDefiner(
			DeclarationStatement statement,
			StatementEnv env) {
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
	public ValueStruct<?, ?> valueStruct(Scope scope) {
		return null;
	}

	@Override
	public final Definitions define(Scope scope) {
		return null;
	}

	@Override
	public final Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

}

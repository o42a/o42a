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
package org.o42a.core.st.impl.imperative;

import static org.o42a.core.object.def.impl.LocalDef.localDef;
import static org.o42a.core.st.DefinitionTarget.valueDefinition;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.ImperativeBlock;


public final class ImperativeDefiner extends Definer {

	private final Command command;

	public ImperativeDefiner(ImperativeBlock block, DefinerEnv env) {
		super(block, env);
		this.command = block.command(new BlockCommandEnv(null, env));
	}

	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) getStatement();
	}

	public final Command getCommand() {
		return this.command;
	}

	@Override
	public DefTargets getDefTargets() {
		return this.command.getCommandTargets().toDefTargets();
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return valueDefinition(getStatement());
	}

	@Override
	public DefinerEnv nextEnv() {
		return new ImperativeDefinerEnv(this);
	}

	@Override
	public Definitions define(Scope scope) {

		final ValueDef localDef = localDef(getBlock(), scope, this.command);

		return env().apply(localDef).toDefinitions(
				env().getExpectedValueStruct());
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

}

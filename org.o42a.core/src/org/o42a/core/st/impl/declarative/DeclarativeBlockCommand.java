/*
    Compiler Core
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
package org.o42a.core.st.impl.declarative;

import org.o42a.core.Scope;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.impl.cmd.BlockCommand;
import org.o42a.core.st.sentence.DeclarativeBlock;


public final class DeclarativeBlockCommand
		extends BlockCommand<DeclarativeBlock>
		implements DefinitionsBuilder {

	private BlockDefinitions blockDefinitions;

	public DeclarativeBlockCommand(DeclarativeBlock block, CommandEnv env) {
		super(block, env);
	}

	public BlockDefinitions getBlockDefinitions() {
		if (this.blockDefinitions != null) {
			return this.blockDefinitions;
		}
		return this.blockDefinitions = new BlockDefinitions(getBlock(), env());
	}

	@Override
	public CommandTargets getTargets() {
		return getBlockDefinitions().getTargets();
	}

	@Override
	public Definitions buildDefinitions() {
		return getBlockDefinitions().buildDefinitions();
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return getSentences().declarativeTarget(origin);
	}

}

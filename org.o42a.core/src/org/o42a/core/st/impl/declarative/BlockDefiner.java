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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.st.Definer;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.impl.BlockImplication;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;


public final class BlockDefiner
		extends BlockImplication<DeclarativeBlock, Definer>
		implements Definer {

	private final DefinerEnv env;
	private DefinitionTargets definitionTargets;

	public BlockDefiner(DeclarativeBlock block, DefinerEnv env) {
		super(block);
		this.env = env;
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		if (this.definitionTargets != null) {
			return this.definitionTargets;
		}
		getBlock().executeInstructions();

		DefinitionTargets result = noDefinitions();

		for (DeclarativeSentence sentence : getBlock().getSentences()) {
			result = result.add(sentence.getDefinitionTargets());
		}

		return this.definitionTargets = result;
	}

	@Override
	public final DefinerEnv env() {
		return this.env;
	}

	@Override
	public DefinerEnv nextEnv() {
		return new DeclarativeBlockEnv(this);
	}

	@Override
	public Definitions define(Scope scope) {
		if (!getDefinitionTargets().haveDefinition()) {
			return null;
		}

		Definitions result = null;

		for (DeclarativeSentence sentence : getBlock().getSentences()) {

			final Definitions definitions = sentence.define(scope);

			if (definitions == null) {
				continue;
			}
			if (result == null) {
				result = definitions;
			} else {
				result = result.refine(definitions);
			}
		}

		assert result != null :
			"Missing definitions: " + this;

		return result;
	}

}

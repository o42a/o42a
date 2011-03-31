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
package org.o42a.core.st.sentence.declarative;

import static org.o42a.core.def.Definitions.emptyDefinitions;

import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;


public final class SentenceDefinitionsCollector extends SentenceCollector {

	private Definitions definitions;

	public SentenceDefinitionsCollector(DeclarativeBlock block, Scope scope) {
		super(block, scope);
	}

	public Definitions definitions() {
		collect();
		if (this.definitions == null) {
			return null;
		}
		this.definitions = this.definitions.addRequirement(requirement());
		this.definitions = this.definitions.addCondition(condition());
		return this.definitions;
	}

	@Override
	protected void addCondition(
			DeclarativeSentence sentence,
			DefinitionTargets targets) {
		if (this.definitions == null) {
			this.definitions = emptyDefinitions(getBlock(), getScope());
		}
	}

	@Override
	protected void addDeclaration(
			DeclarativeSentence sentence,
			DefinitionTargets targets) {
		if (!targets.haveValue()) {
			return;
		}

		final Definitions definitions = sentence.define(getScope());

		assert definitions != null :
			sentence + " has no definitions";

		if (this.definitions == null) {
			this.definitions = definitions;
		} else {
			this.definitions = this.definitions.refine(definitions);
		}
	}

}

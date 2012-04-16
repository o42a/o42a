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
import org.o42a.core.ref.Resolver;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;


public final class BlockDefiner extends Definer {

	private DefTargets targets;
	private DefinitionTargets definitionTargets;

	public BlockDefiner(DeclarativeBlock block, DefinerEnv env) {
		super(block, env);
	}

	public final DeclarativeBlock getBlock() {
		return (DeclarativeBlock) getStatement();
	}

	@Override
	public DefTargets getDefTargets() {
		if (this.targets != null) {
			return this.targets;
		}
		getBlock().executeInstructions();
		return this.targets = sentenceTargets();
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

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	private DefTargets sentenceTargets() {

		DefTargets result = noDefs();
		DefTargets prev = noDefs();

		for (DeclarativeSentence sentence : getBlock().getSentences()) {

			final DefTargets targets = sentence.getDefTargets();

			if (!targets.defining()) {
				continue;
			}
			if (targets.isClaim() && result.defining() && !result.isClaim()) {
				if (!result.haveError()) {
					getLogger().error(
							"prohibited_claim_after_proposition",
							sentence,
							"Claims should never follow propositions");
					result = result.addError();
				}
			}
			if (!prev.breaking() || prev.havePrerequisite()) {
				if (targets.breaking()) {
					prev = targets;
				} else {
					prev = targets.toPreconditions();
				}
				result = result.add(prev);
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"redundant_sentence",
					targets,
					"Redundant sentence");
		}

		return result;
	}

}

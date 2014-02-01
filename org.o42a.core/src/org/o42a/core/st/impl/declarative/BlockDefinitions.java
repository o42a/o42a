/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.st.Command.noCommands;

import java.util.ArrayList;

import org.o42a.core.object.def.Definitions;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.TypeParameters;


final class BlockDefinitions {

	private final DeclarativeBlock block;
	private final CommandEnv env;
	private CommandTargets targets = noCommands();
	private CommandTargets claimTargets = noCommands();
	private CommandTargets propositionTargets = noCommands();
	private ArrayList<DeclarativeSentence> claims;
	private ArrayList<DeclarativeSentence> propositions;

	BlockDefinitions(DeclarativeBlock block, CommandEnv env) {
		this.block = block;
		this.env = env;
		build();
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	public final CommandEnv env() {
		return this.env;
	}

	public final CommandTargets getTargets() {
		return this.targets;
	}

	public final ArrayList<DeclarativeSentence> getClaims() {
		return this.claims;
	}

	public final ArrayList<DeclarativeSentence> getPropositions() {
		return this.propositions;
	}

	public final CompilerLogger getLogger() {
		return getBlock().getLogger();
	}

	public Definitions buildDefinitions() {

		final TypeParameters<?> typeParameters =
				env().getValueRequest().getExpectedParameters();
		final Definitions claims;

		if (this.claims == null) {
			claims = typeParameters.noValueDefinitions(
					getBlock(),
					getBlock().getScope());
		} else {
			claims =
					new DeclarativePart(
							getBlock(),
							env(),
							this.claimTargets,
							this.claims,
							true)
					.toDefinitions(typeParameters);
		}

		if (this.propositions == null) {
			return claims;
		}

		return claims.refine(
				new DeclarativePart(
						getBlock(),
						env(),
						this.propositionTargets,
						this.propositions,
						false)
				.toDefinitions(typeParameters));
	}

	@Override
	public String toString() {
		if (this.block == null) {
			return super.toString();
		}
		return this.block.toString();
	}

	private void build() {
		getBlock().executeInstructions();

		int index = -1;

		for (DeclarativeSentence sentence : getBlock().getSentences()) {
			++index;

			final CommandTargets targets = sentence.getTargets();

			if (!targets.defining()) {
				continue;
			}

			final boolean claim;

			if (targets.isClaim()
					&& this.targets.defining()
					&& !this.targets.isClaim()) {
				if (!this.targets.haveError()) {
					getLogger().error(
							"prohibited_claim_after_proposition",
							sentence,
							"Claims should never follow propositions");
					this.targets = this.targets.addError();
				}
				claim = false;
			} else {
				claim = targets.isClaim();
			}
			if (!this.targets.breaking() || this.targets.havePrerequisite()) {
				addSentence(sentence, targets, index, claim);
				continue;
			}
			if (this.targets.haveError()) {
				continue;
			}
			this.targets = this.targets.addError();
			getLogger().error(
					"redundant_sentence",
					targets,
					"Redundant sentence");
		}
	}

	private void addSentence(
			DeclarativeSentence sentence,
			CommandTargets targets,
			int index,
			boolean claim) {
		if (claim) {
			if (this.claims == null) {
				this.claims = new ArrayList<>(
						getBlock().getSentences().size() - index);
			}
			this.claims.add(sentence);
		} else {
			if (this.propositions == null) {
				this.propositions = new ArrayList<>(
						getBlock().getSentences().size() - index);
			}
			this.propositions.add(sentence);
		}

		if (!targets.breaking()) {
			addTargets(targets.toPreconditions(), claim);
		} else {
			addTargets(targets, claim);
			if (!targets.havePrerequisite()) {
				this.targets = this.targets.toPreconditions();
				if (claim) {
					this.claimTargets = this.claimTargets.toPreconditions();
				} else {
					this.propositionTargets =
							this.propositionTargets.toPreconditions();
				}
			}
		}
	}

	private void addTargets(CommandTargets targets, boolean claim) {
		this.targets = this.targets.add(targets);
		if (claim) {
			this.claimTargets = this.claimTargets.add(targets);
		} else {
			this.propositionTargets = this.propositionTargets.add(targets);
		}
	}

}

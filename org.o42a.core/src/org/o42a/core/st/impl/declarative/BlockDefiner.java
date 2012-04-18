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

import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;
import static org.o42a.core.st.DefinitionTargets.noDefinitions;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.value.ValueStruct;


public final class BlockDefiner
		extends Definer
		implements DeclarativeSentences {

	static DefValue sentencesValue(
			Resolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {

			final DefValue value = sentence.value(resolver);

			if (value.hasValue()) {
				return value;
			}
			if (!value.getLogicalValue().isTrue()) {
				return value;
			}
		}

		return TRUE_DEF_VALUE;
	}

	static void resolveSentences(
			Resolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {
			resolveSentence(resolver, sentence);
		}
	}

	static void normalizeSentences(
			RootNormalizer normalizer,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {
			normalizeSentence(normalizer, sentence);
		}
	}

	private DefTargets targets;
	private DefinitionTargets definitionTargets;
	private ArrayList<DeclarativeSentence> claims;
	private ArrayList<DeclarativeSentence> propositions;

	public BlockDefiner(DeclarativeBlock block, DefinerEnv env) {
		super(block, env);
		this.claims = new ArrayList<DeclarativeSentence>(
				block.getSentences().size());
		this.propositions = new ArrayList<DeclarativeSentence>(
				block.getSentences().size());
	}

	public final DeclarativeBlock getBlock() {
		return (DeclarativeBlock) getStatement();
	}

	@Override
	public final List<DeclarativeSentence> getSentences() {
		return getBlock().getSentences();
	}

	public final List<DeclarativeSentence> getClaims() {
		getDefTargets();
		return this.claims;
	}

	public final List<DeclarativeSentence> getPropositions() {
		getDefTargets();
		return this.propositions;
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
	public DefValue value(Resolver resolver) {
		return sentencesValue(resolver, this);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public InlineEval inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		normalizeSentences(normalizer, this);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getDefTargets();
		resolveSentences(resolver, this);
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	private DefTargets sentenceTargets() {

		DefTargets result = noDefs();
		DefTargets prev = noDefs();

		for (DeclarativeSentence sentence : getBlock().getSentences()) {

			final DefTargets targets = sentence.getDefTargets();

			if (!targets.defining()) {
				continue;
			}

			final boolean claim;

			if (targets.isClaim() && result.defining() && !result.isClaim()) {
				if (!result.haveError()) {
					getLogger().error(
							"prohibited_claim_after_proposition",
							sentence,
							"Claims should never follow propositions");
					result = result.addError();
				}
				claim = false;
			} else {
				claim = targets.isClaim();
			}
			if (!prev.breaking() || prev.havePrerequisite()) {
				if (targets.breaking()) {
					prev = targets;
				} else {
					prev = targets.toPreconditions();
				}
				result = result.add(prev);
				if (claim) {
					this.claims.add(sentence);
				} else {
					this.propositions.add(sentence);
				}
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

	private static void resolveSentence(
			Resolver resolver,
			DeclarativeSentence sentence) {

		final DeclarativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			resolveSentence(resolver, prerequisite);
		}
		for (Declaratives alt : sentence.getAlternatives()) {
			resolveStatements(resolver, alt);
		}
	}

	private static void resolveStatements(
			Resolver resolver,
			Declaratives statements) {
		assert statements.assertInstructionsExecuted();
		for (Definer command : statements.getImplications()) {
			command.resolveAll(resolver);
		}
	}

	private static void normalizeSentence(
			RootNormalizer normalizer,
			DeclarativeSentence sentence) {

		final DeclarativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			normalizeSentence(normalizer, prerequisite);
		}
		for (Declaratives alt : sentence.getAlternatives()) {
			normalizeCommands(normalizer, alt);
		}
	}

	private static void normalizeCommands(
			RootNormalizer normalizer,
			Declaratives statements) {
		for (Definer definer : statements.getImplications()) {
			definer.normalize(normalizer);
		}
	}

}

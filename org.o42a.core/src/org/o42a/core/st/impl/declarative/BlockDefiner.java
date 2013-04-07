/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;
import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentences.inlineBlock;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


public final class BlockDefiner
		extends MainDefiner
		implements DeclarativeSentences {

	static TypeParameters<?> typeParameters(
			Scope scope,
			DeclarativeSentences sentences,
			TypeParameters<?> expectedParameters) {

		TypeParameters<?> typeParameters = null;

		for (DeclarativeSentence sentence : sentences.getSentences()) {

			final TypeParameters<?> sentenceParameters =
					sentence.typeParameters(scope, expectedParameters);

			if (sentenceParameters == null) {
				continue;
			}
			if (typeParameters == null) {
				typeParameters = sentenceParameters;
				continue;
			}
			if (typeParameters.assignableFrom(sentenceParameters)) {
				continue;
			}
			if (sentenceParameters.assignableFrom(typeParameters)) {
				typeParameters = sentenceParameters;
				continue;
			}
			typeParameters = expectedParameters;
		}

		return typeParameters;
	}

	static DefValue sentencesValue(
			Resolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {

			final DefValue value = sentence.value(resolver);

			if (value.hasValue()) {
				return value;
			}
			if (!value.getCondition().isTrue()) {
				return value;
			}
		}

		return TRUE_DEF_VALUE;
	}

	static DefTarget sentencesTargets(
			Scope origin,
			DeclarativeSentences sentences) {

		final DefTargets defTargets = sentences.getDefTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		for (DeclarativeSentence sentence : sentences.getSentences()) {

			final DefTarget sentenceTarget = sentenceTarget(origin, sentence);

			if (sentenceTarget != null) {
				return sentenceTarget;
			}
		}

		return null;
	}

	static void resolveSentences(
			FullResolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {
			resolveSentence(resolver, sentence);
		}
	}

	static void resolveSentencesTargets(
			TargetResolver resolver,
			Scope scope,
			DeclarativeSentences sentences) {
		if (!sentences.getDefTargets().haveValue()) {
			return;
		}
		for (DeclarativeSentence sentence : sentences.getSentences()) {
			resolveSentenceTargets(resolver, scope, sentence);
		}
	}

	private BlockDefinitions blockDefinitions;

	public BlockDefiner(DeclarativeBlock block, CommandEnv env) {
		super(block, env);
	}

	@Override
	public final List<DeclarativeSentence> getSentences() {
		return getBlock().getSentences();
	}

	@Override
	public final DefTargets getDefTargets() {
		return getBlockDefinitions().getTargets();
	}

	@Override
	public Definitions createDefinitions() {
		return getBlockDefinitions().createDefinitions();
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return sentencesTargets(origin, this);
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return typeParameters(
				scope,
				this,
				env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope));
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
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		resolveSentencesTargets(resolver, origin, this);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return inlineBlock(normalizer.getRoot(), normalizer, origin, this);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inlineBlock(normalizer, null, origin, this);
	}

	@Override
	public Eval eval(CodeBuilder builder, Scope origin) {
		assert getStatement().assertFullyResolved();
		return new BlockEval(this, origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getDefTargets();
		resolveSentences(resolver, this);
	}

	private BlockDefinitions getBlockDefinitions() {
		if (this.blockDefinitions != null) {
			return this.blockDefinitions;
		}
		return this.blockDefinitions = new BlockDefinitions(this);
	}

	private static DefTarget sentenceTarget(
			Scope origin,
			DeclarativeSentence sentence) {

		final DefTargets defTargets = sentence.getDefTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		final List<Declaratives> alts = sentence.getAlternatives();
		final int size = alts.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return NO_DEF_TARGET;
		}

		return statementsTarget(origin, alts.get(0));
	}

	private static DefTarget statementsTarget(
			Scope origin,
			Declaratives statements) {

		final DefTargets defTargets = statements.getDefTargets();

		if (!defTargets.defining()) {
			return null;
		}
		if (defTargets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}

		final List<Definer> definers = statements.getImplications();
		final int size = definers.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return NO_DEF_TARGET;
		}

		return definers.get(0).toTarget(origin);
	}

	private static void resolveSentence(
			FullResolver resolver,
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
			FullResolver resolver,
			Declaratives statements) {
		assert statements.assertInstructionsExecuted();
		for (Definer definer : statements.getImplications()) {
			definer.resolveAll(resolver);
		}
	}

	private static void resolveSentenceTargets(
			TargetResolver resolver,
			Scope scope,
			DeclarativeSentence sentence) {
		if (!sentence.getDefTargets().haveValue()) {
			return;
		}
		for (Declaratives alt : sentence.getAlternatives()) {
			resolveStatementsTargets(resolver, scope, alt);
		}
	}

	private static void resolveStatementsTargets(
			TargetResolver resolver,
			Scope scope,
			Declaratives statements) {
		assert statements.assertInstructionsExecuted();
		for (Definer definer : statements.getImplications()) {
			definer.resolveTargets(resolver, scope);
		}
	}

	private static final class BlockEval implements Eval {

		private final BlockDefiner definer;
		private final Scope origin;

		BlockEval(BlockDefiner definer, Scope origin) {
			this.definer = definer;
			this.origin = origin;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			writeSentences(dirs, host, this.origin, this.definer, null);
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
		}

	}

}

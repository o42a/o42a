/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;
import static org.o42a.core.st.impl.imperative.InlineImperativeBlock.inlineBlock;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.Condition;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;


public final class BlockCommand extends Command {

	public static Action blockAction(
			Block<?, ?> block,
			Implication<?> implication,
			Resolver resolver) {
		if (implication.getTargets().isEmpty()) {
			return new ExecuteCommand(implication, Condition.TRUE);
		}

		for (Sentence<?, ?> sentence : block.getSentences()) {

			final Action action = sentenceAction(sentence, resolver);
			final LoopAction loopAction = action.toLoopAction(block);

			switch (loopAction) {
			case CONTINUE:
				continue;
			case PULL:
				return action;
			case EXIT:
				return new ExecuteCommand(action, action.getCondition());
			case REPEAT:
				// Repeating is not supported at compile time.
				return new ExecuteCommand(implication, Condition.RUNTIME);
			}

			throw new IllegalStateException("Unhandled action: " + action);
		}

		return new ExecuteCommand(implication, Condition.TRUE);
	}

	private CommandTargets commandTargets;

	public BlockCommand(ImperativeBlock block, CommandEnv env) {
		super(block, env);
	}

	public final ImperativeBlock getBlock() {
		return (ImperativeBlock) getStatement();
	}

	@Override
	public CommandTargets getTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}

		getBlock().executeInstructions();

		return this.commandTargets = applyBraceTargets(sentenceTargets());
	}

	@Override
	public DefTarget toTarget(Scope origin) {

		final CommandTargets targets = getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		for (ImperativeSentence sentence : getBlock().getSentences()) {

			final DefTarget target = sentenceTarget(origin, sentence);

			if (target != null) {
				return target;
			}
		}

		return null;
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {

		TypeParameters<?> typeParameters = null;
		final TypeParameters<?> expectedParameters =
				env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);

		for (ImperativeSentence sentence : getBlock().getSentences()) {

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
			typeParameters = sentenceParameters;
		}

		return typeParameters;
	}

	@Override
	public Action action(Resolver resolver) {
		return blockAction(getBlock(), this, resolver);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		if (!getTargets().haveValue()) {
			return;
		}
		for (ImperativeSentence sentence : getBlock().getSentences()) {
			resolveSentenceTargets(resolver, origin, sentence);
		}
	}

	@Override
	public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
		return inlineBlock(
				normalizer.getRoot(),
				normalizer,
				origin,
				getBlock());
	}

	@Override
	public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
		return inlineBlock(normalizer, null, origin, getBlock());
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public Cmd cmd() {
		assert getStatement().assertFullyResolved();
		return new ImperativeBlockCmd(getBlock());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getTargets();
		for (ImperativeSentence sentence : getBlock().getSentences()) {
			resolveSentence(resolver, sentence);
		}
	}

	private CommandTargets sentenceTargets() {

		CommandTargets result = noCommands();

		for (ImperativeSentence sentence : getBlock().getSentences()) {

			final CommandTargets targets = sentence.getTargets();

			if (!result.breaking() || result.havePrerequisite()) {
				if (!targets.breaking()) {
					result = result.add(targets.toPreconditions());
				} else {
					result = result.add(targets);
					if (!targets.havePrerequisite()) {
						result = result.toPreconditions();
					}
				}
				continue;
			}
			if (result.haveError()) {
				continue;
			}
			result = result.addError();
			getLogger().error(
					"unreachable_sentence",
					targets,
					"Unreachable sentence");
		}

		return result;
	}

	private CommandTargets applyBraceTargets(CommandTargets targets) {
		if (getBlock().isParentheses()) {
			return targets;
		}
		return targets.removeLooping();
	}

	private static Action sentenceAction(
			Sentence<?, ?> sentence,
			Resolver resolver) {
		if (sentence.getTargets().isEmpty()) {
			return new ExecuteCommand(sentence, Condition.TRUE);
		}

		final Sentence<?, ?> prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {

			final Action action = sentenceAction(prerequisite, resolver);

			assert !action.isAbort() :
				"Prerequisite can not abort execution";

			final Condition condition = action.getCondition();

			if (!condition.isConstant()) {
				// Can not go on.
				return action;
			}
			if (condition.isFalse()) {
				// Skip this sentence, as it`s prerequisite not satisfied.
				return new ExecuteCommand(sentence, Condition.TRUE);
			}
		}

		final List<? extends Statements<?, ?>> alternatives =
				sentence.getAlternatives();
		final int size = alternatives.size();
		Action result = null;

		for (int i = 0; i < size; ++i) {

			final Statements<?, ?> alt = alternatives.get(i);
			final Action action = altAction(alt, resolver);

			if (action.isAbort()) {
				return action;
			}

			final Condition condition = action.getCondition();

			if (!condition.isConstant()) {
				// can not go on
				return action;
			}
			if (result != null && !result.getCondition().isTrue()) {
				return result;
			}

			result = action;
		}

		if (!sentence.getSentenceFactory().isDeclarative()) {
			if (sentence.isClaim()) {
				return new ExitLoop(sentence, null);
			}
		}
		if (result != null) {
			return result;
		}

		return new ExecuteCommand(sentence, Condition.TRUE);
	}

	private static Action altAction(Statements<?, ?> alt, Resolver resolver) {

		Action result = null;

		for (Implication<?> command : alt.getImplications()) {

			final Action action = command.action(resolver);

			if (action.isAbort()) {
				return action;
			}
			if (action.getCondition().isFalse()) {
				return action;
			}

			result = action;
		}

		if (result != null) {
			return result;
		}

		return new ExecuteCommand(alt, Condition.TRUE);
	}

	private static DefTarget sentenceTarget(
			Scope origin,
			ImperativeSentence sentence) {

		final CommandTargets targets = sentence.getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		final List<Imperatives> alts = sentence.getAlternatives();
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
			Imperatives statements) {

		final CommandTargets targets = statements.getTargets();

		if (targets.isEmpty()) {
			return null;
		}
		if (targets.havePrerequisite()) {
			return NO_DEF_TARGET;
		}
		if (targets.looping()) {
			return NO_DEF_TARGET;
		}
		if (!targets.haveValue()) {
			return NO_DEF_TARGET;
		}

		final List<Command> commands = statements.getImplications();
		final int size = commands.size();

		if (size != 1) {
			if (size == 0) {
				return null;
			}
			return commands.get(0).toTarget(origin);
		}

		return NO_DEF_TARGET;
	}

	private static void resolveSentence(
			FullResolver resolver,
			ImperativeSentence sentence) {

		final ImperativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			resolveSentence(resolver, prerequisite);
		}
		for (Imperatives alt : sentence.getAlternatives()) {
			resolveStatements(resolver, alt);
		}
	}

	private static void resolveStatements(
			FullResolver resolver,
			Imperatives imperatives) {
		assert imperatives.assertInstructionsExecuted();
		for (Command command : imperatives.getImplications()) {
			command.resolveAll(resolver);
		}
	}

	private static void resolveSentenceTargets(
			TargetResolver resolver,
			Scope origin,
			ImperativeSentence sentence) {
		if (!sentence.getTargets().haveValue()) {
			return;
		}
		for (Imperatives alt : sentence.getAlternatives()) {
			resolveStatementsTargets(resolver, origin, alt);
		}
	}

	private static void resolveStatementsTargets(
			TargetResolver resolver,
			Scope origin,
			Imperatives statements) {
		assert statements.assertInstructionsExecuted();
		for (Command command : statements.getImplications()) {
			command.resolveTargets(resolver, origin);
		}
	}

}

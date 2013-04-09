/*
    Compiler Core
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.core.st.impl.cmd;

import static org.o42a.core.st.impl.cmd.InlineSentence.inlineSentence;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.action.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.Condition;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public abstract class Sentences {

	public abstract Name getName();

	public abstract boolean isParentheses();

	public abstract List<? extends Sentence<?>> getSentences();

	public abstract CommandTargets getTargets();

	public TypeParameters<?> typeParameters(
			Scope scope,
			TypeParameters<?> expectedParameters) {

		TypeParameters<?> typeParameters = null;

		for (Sentence<?> sentence : getSentences()) {

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
			typeParameters = expectedParameters;
		}

		return typeParameters;
	}

	public Action action(
			ScopeInfo location,
			Block<?> block,
			Resolver resolver) {
		if (getTargets().isEmpty()) {
			return new ExecuteCommand(location, Condition.TRUE);
		}

		for (Sentence<?> sentence : getSentences()) {

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
				return new ExecuteCommand(location, Condition.RUNTIME);
			}

			throw new IllegalStateException("Unhandled action: " + action);
		}

		return new ExecuteCommand(location, Condition.TRUE);
	}

	public void resolveAll(FullResolver resolver) {
		for (Sentence<?> sentence : getSentences()) {
			resolveSentence(resolver, sentence);
		}
	}

	public void resolveTargets(TargetResolver resolver, Scope scope) {
		if (!getTargets().haveValue()) {
			return;
		}
		for (Sentence<?> sentence : getSentences()) {
			resolveSentenceTargets(resolver, scope, sentence);
		}
	}

	public InlineSentences inline(
			RootNormalizer rootNormalizer,
			Normalizer normalizer,
			Scope origin) {

		final List<? extends Sentence<?>> sentenceList =
				getSentences();
		final InlineSentence[] inlines =
				new InlineSentence[sentenceList.size()];
		int i = 0;

		for (Sentence<?> sentence : sentenceList) {
			inlines[i++] = inlineSentence(
					rootNormalizer,
					normalizer,
					origin,
					sentence);
		}

		if (normalizer != null && normalizer.isCancelled()) {
			return null;
		}

		return new InlineSentences(this, origin, inlines);
	}

	public final Cmd cmd(Scope origin) {
		return new SentencesCmd(this, origin);
	}

	private static Action sentenceAction(
			Sentence<?> sentence,
			Resolver resolver) {
		if (sentence.getTargets().isEmpty()) {
			return new ExecuteCommand(sentence, Condition.TRUE);
		}

		final Sentence<?> prerequisite = sentence.getPrerequisite();

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

		final List<? extends Statements<?>> alternatives =
				sentence.getAlternatives();
		final int size = alternatives.size();
		Action result = null;

		for (int i = 0; i < size; ++i) {

			final Statements<?> alt = alternatives.get(i);
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

		if (sentence.isExit()) {
			return new ExitLoop(sentence, null);
		}
		if (result != null) {
			return result;
		}

		return new ExecuteCommand(sentence, Condition.TRUE);
	}

	private static Action altAction(Statements<?> alt, Resolver resolver) {

		Action result = null;

		for (Command command : alt.getCommands()) {

			final Action action = command.action(resolver);

			if (action.isAbort()) {
				return action;
			}
			if (!action.getCondition().isTrue()) {
				return action;
			}

			result = action;
		}

		if (result != null) {
			return result;
		}

		return new ExecuteCommand(alt, Condition.TRUE);
	}

	private static void resolveSentence(
			FullResolver resolver,
			Sentence<?> sentence) {

		final Sentence<?> prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			resolveSentence(resolver, prerequisite);
		}
		for (Statements<?> alt : sentence.getAlternatives()) {
			resolveStatements(resolver, alt);
		}
	}

	private static void resolveStatements(
			FullResolver resolver,
			Statements<?> statements) {
		assert statements.assertInstructionsExecuted();
		for (Command command : statements.getCommands()) {
			command.resolveAll(resolver);
		}
	}

	private static void resolveSentenceTargets(
			TargetResolver resolver,
			Scope scope,
			Sentence<?> sentence) {
		if (!sentence.getTargets().haveValue()) {
			return;
		}
		for (Statements<?> alt : sentence.getAlternatives()) {
			resolveStatementsTargets(resolver, scope, alt);
		}
	}

	private static void resolveStatementsTargets(
			TargetResolver resolver,
			Scope scope,
			Statements<?> statements) {
		assert statements.assertInstructionsExecuted();
		for (Command command : statements.getCommands()) {
			command.resolveTargets(resolver, scope);
		}
	}

}

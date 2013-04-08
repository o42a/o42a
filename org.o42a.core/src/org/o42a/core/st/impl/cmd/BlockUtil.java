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

import java.util.List;

import org.o42a.core.ref.Resolver;
import org.o42a.core.st.Implication;
import org.o42a.core.st.action.*;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.Condition;


public class BlockUtil {

	public static Action sentencesAction(
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

		if (sentence.isExit()) {
			return new ExitLoop(sentence, null);
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

	private BlockUtil() {
	}

}

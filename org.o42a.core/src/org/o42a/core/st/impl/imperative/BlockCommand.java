/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import static org.o42a.core.st.CommandTarget.noCommand;
import static org.o42a.core.st.impl.imperative.InlineBlock.inlineBlock;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.action.*;
import org.o42a.core.st.impl.BlockImplication;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.ValueStruct;


public final class BlockCommand
		extends BlockImplication<ImperativeBlock, Command>
		implements Command {

	private final CommandEnv env;
	private CommandTarget commandTarget;

	public BlockCommand(ImperativeBlock block, CommandEnv env) {
		super(block);
		this.env = env;
	}

	@Override
	public final CommandEnv env() {
		return this.env;
	}

	@Override
	public final ImplicationTarget getImplicationTarget() {
		return getCommandTarget();
	}

	@Override
	public CommandTarget getCommandTarget() {
		if (this.commandTarget != null) {
			return this.commandTarget;
		}
		getBlock().executeInstructions();

		CommandTarget result = noCommand();

		for (ImperativeSentence sentence : getBlock().getSentences()) {
			result = result.combine(sentence.getCommandTarget());
		}

		return this.commandTarget = result;
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		for (ImperativeSentence sentence : getBlock().getSentences()) {

			final Action action = initialValue(sentence, resolver);
			final LoopAction loopAction = action.toLoopAction(getBlock());

			switch (loopAction) {
			case CONTINUE:
				continue;
			case PULL:
				return action;
			case EXIT:
				return new ExecuteCommand(action, action.getLogicalValue());
			case REPEAT:
				// Repeating is not supported at compile time.
				return new ExecuteCommand(this, LogicalValue.RUNTIME);
			}

			throw new IllegalStateException("Unhandled action: " + action);
		}

		return new ExecuteCommand(this, LogicalValue.TRUE);
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		return initialValue(resolver).toInitialLogicalValue();
	}

	@Override
	public void resolveAll(LocalResolver resolver) {
		getBlock().blockFullyResolved();
		getContext().fullResolution().start();
		try {
			getCommandTarget();
			for (ImperativeSentence sentence : getBlock().getSentences()) {
				resolveSentence(resolver, sentence);
			}
		} finally {
			getContext().fullResolution().end();
		}
	}

	@Override
	public InlineCmd inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return inlineBlock(normalizer, valueStruct, origin, getBlock());
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		for (ImperativeSentence sentence : getBlock().getSentences()) {
			normalizeSentence(normalizer, sentence);
		}
	}

	@Override
	public Cmd cmd(CodeBuilder builder) {
		return new ImperativeBlockCmd(builder, getBlock());
	}

	private Action initialValue(
			ImperativeSentence sentence,
			LocalResolver resolver) {

		final ImperativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {

			final Action action = initialValue(prerequisite, resolver);

			assert !action.isAbort() :
				"Prerequisite can not abort execution";

			final LogicalValue logicalValue = action.getLogicalValue();

			if (!logicalValue.isConstant()) {
				// Can not go on.
				return action;
			}
			if (logicalValue.isFalse()) {
				// Skip this sentence, as it`s prerequisite not satisfied.
				return new ExecuteCommand(sentence, LogicalValue.TRUE);
			}
		}

		final List<Imperatives> alternatives = sentence.getAlternatives();
		final int size = alternatives.size();
		Action result = null;

		for (int i = 0; i < size; ++i) {

			final Imperatives alt = alternatives.get(i);
			final Action action = initialValue(alt, resolver);

			if (action.isAbort()) {
				return action;
			}

			final LogicalValue logicalValue = action.getLogicalValue();

			if (!logicalValue.isConstant()) {
				// can not go on
				return action;
			}
			if (!alt.isOpposite()) {
				if (result != null && !result.getLogicalValue().isTrue()) {
					return result;
				}
			} else if (!sentence.hasOpposite(i)) {
				if (!action.getLogicalValue().isTrue()) {
					return action;
				}
			}

			result = action;
		}

		if (sentence.isClaim()) {
			return new ExitLoop(sentence, null);
		}
		if (result != null) {
			return result;
		}

		return new ExecuteCommand(sentence, LogicalValue.TRUE);
	}

	private Action initialValue(Imperatives alt, LocalResolver resolver) {

		Action result = null;

		for (Command command : alt.getImplications()) {

			final Action action = command.initialValue(resolver);

			if (action.isAbort()) {
				return action;
			}
			if (!action.getLogicalValue().isConstant()) {
				return action;
			}

			result = action;
		}

		if (result != null) {
			return result;
		}

		return new ExecuteCommand(alt, LogicalValue.TRUE);
	}

	private final void resolveSentence(
			LocalResolver resolver,
			ImperativeSentence sentence) {

		final ImperativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			resolveSentence(resolver, prerequisite);
		}
		for (Imperatives alt : sentence.getAlternatives()) {
			resolveCommands(resolver, alt);
		}
	}

	private void resolveCommands(
			LocalResolver resolver,
			Imperatives imperatives) {
		assert imperatives.assertInstructionsExecuted();
		for (Command command : imperatives.getImplications()) {
			command.resolveAll(resolver);
		}
	}

	private void normalizeSentence(
			RootNormalizer normalizer,
			ImperativeSentence sentence) {

		final ImperativeSentence prerequisite = sentence.getPrerequisite();

		if (prerequisite != null) {
			normalizeSentence(normalizer, prerequisite);
		}
		for (Imperatives alt : sentence.getAlternatives()) {
			normalizeCommands(normalizer, alt);
		}
	}

	private void normalizeCommands(
			RootNormalizer normalizer,
			Imperatives imperatives) {
		for (Command command : imperatives.getImplications()) {
			command.normalize(normalizer);
		}
	}

}

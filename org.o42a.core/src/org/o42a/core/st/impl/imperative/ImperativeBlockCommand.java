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

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.impl.cmd.Sentences;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public final class ImperativeBlockCommand extends Command {

	private final ImperativeSentences sentences = new ImperativeSentences(this);
	private CommandTargets commandTargets;

	public ImperativeBlockCommand(ImperativeBlock block, CommandEnv env) {
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

		final TypeParameters<?> expectedParameters =
				env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);

		return this.sentences.typeParameters(scope, expectedParameters);
	}

	@Override
	public Action action(Resolver resolver) {
		return this.sentences.action(this, getBlock(), resolver);
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		if (!getTargets().haveValue()) {
			return;
		}
		this.sentences.resolveTargets(resolver, origin);
	}

	@Override
	public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
		return this.sentences.inline(normalizer.getRoot(), normalizer, origin);
	}

	@Override
	public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
		return this.sentences.inline(normalizer, null, origin);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return this.sentences.cmd(origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getTargets();
		this.sentences.resolveAll(resolver);
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

	private static final class ImperativeSentences extends Sentences {

		private final ImperativeBlockCommand command;

		ImperativeSentences(ImperativeBlockCommand command) {
			this.command = command;
		}

		@Override
		public Name getName() {
			return this.command.getBlock().getName();
		}

		@Override
		public boolean isParentheses() {
			return this.command.getBlock().isParentheses();
		}

		@Override
		public CommandTargets getTargets() {
			return this.command.getTargets();
		}

		@Override
		public List<? extends Sentence<?>> getSentences() {
			return this.command.getBlock().getSentences();
		}

		@Override
		public String toString() {
			if (this.command == null) {
				return super.toString();
			}
			return this.command.toString();
		}

	}

}

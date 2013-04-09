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
package org.o42a.core.st.impl.declarative;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.local.Cmd;
import org.o42a.core.ir.local.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.impl.cmd.SentencesCmd;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public final class DeclarativeBlockCommand
		extends Command
		implements DefinitionsBuilder {

	private final DeclarativeBlockSentences sentences =
			new DeclarativeBlockSentences(this);
	private BlockDefinitions blockDefinitions;

	public DeclarativeBlockCommand(DeclarativeBlock block, CommandEnv env) {
		super(block, env);
	}

	public final DeclarativeBlock getBlock() {
		return (DeclarativeBlock) getStatement();
	}

	public BlockDefinitions getBlockDefinitions() {
		if (this.blockDefinitions != null) {
			return this.blockDefinitions;
		}
		return this.blockDefinitions = new BlockDefinitions(getBlock(), env());
	}

	@Override
	public CommandTargets getTargets() {
		return getBlockDefinitions().getTargets();
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
	public Definitions buildDefinitions() {
		return getBlockDefinitions().buildDefinitions();
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		this.sentences.resolveTargets(resolver, origin);
	}

	@Override
	public InlineCmd inlineCmd(Normalizer normalizer, Scope origin) {
		return this.sentences.inline(
				normalizer.getRoot(),
				normalizer,
				origin);
	}

	@Override
	public InlineCmd normalizeCmd(RootNormalizer normalizer, Scope origin) {
		return this.sentences.inline(normalizer, null, origin);
	}

	@Override
	public Cmd cmd(Scope origin) {
		return new SentencesCmd(this.sentences, origin);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return this.sentences.target(origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getTargets();
		this.sentences.resolveAll(resolver);
	}

	private static final class DeclarativeBlockSentences
			extends DeclarativeSentences {

		private final DeclarativeBlockCommand command;

		DeclarativeBlockSentences(DeclarativeBlockCommand command) {
			this.command = command;
		}

		@Override
		public Name getName() {
			return null;
		}

		@Override
		public boolean isParentheses() {
			return true;
		}

		@Override
		public List<DeclarativeSentence> getSentences() {
			return this.command.getBlock().getSentences();
		}

		@Override
		public CommandTargets getTargets() {
			return this.command.getTargets();
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

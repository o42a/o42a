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

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public abstract class BlockCommand<B extends Block<?>> extends Command {

	private final BlockSentences sentences;

	public BlockCommand(B block, CommandEnv env) {
		super(block, env);
		this.sentences = new BlockSentences(this);
	}

	@SuppressWarnings("unchecked")
	public final B getBlock() {
		return (B) getStatement();
	}

	public final Sentences getSentences() {
		return this.sentences;
	}

	@Override
	public final TypeParameters<?> typeParameters(Scope scope) {

		final TypeParameters<?> expectedParameters =
				env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);

		return this.sentences.typeParameters(scope, expectedParameters);
	}

	@Override
	public final Action action(Resolver resolver) {
		return this.sentences.action(this, getBlock(), resolver);
	}

	@Override
	public final void resolveTargets(TargetResolver resolver, Scope origin) {
		if (!getTargets().haveValue()) {
			return;
		}
		this.sentences.resolveTargets(resolver, origin);
	}

	@Override
	public final InlineCmd inline(Normalizer normalizer, Scope origin) {
		return this.sentences.inline(normalizer.getRoot(), normalizer, origin);
	}

	@Override
	public final InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return this.sentences.inline(normalizer, null, origin);
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public final Cmd cmd(Scope origin) {
		assert getStatement().assertFullyResolved();
		return this.sentences.cmd(origin);
	}

	@Override
	protected final void fullyResolve(FullResolver resolver) {
		getTargets();
		this.sentences.resolveAll(resolver);
	}

	private static final class BlockSentences extends Sentences {

		private final BlockCommand<?> command;

		BlockSentences(BlockCommand<?> command) {
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

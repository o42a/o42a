/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.st.impl.flow;

import static org.o42a.core.object.def.DefTarget.NO_DEF_TARGET;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.InlineCmd;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.st.impl.cmd.Sentences;
import org.o42a.core.st.sentence.FlowBlock;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public class FlowCommand extends Command {

	private final FlowSentences sentences;
	private CommandTargets commandTargets;

	public FlowCommand(FlowBlock block, CommandEnv env) {
		super(block, env);
		this.sentences = new FlowSentences(this);
	}

	public final FlowBlock getBlock() {
		return (FlowBlock) getStatement();
	}

	@Override
	public CommandTargets getTargets() {
		if (this.commandTargets != null) {
			return this.commandTargets;
		}

		getBlock().executeInstructions();

		return this.commandTargets = returnCommand();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return env()
				.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);
	}

	@Override
	public Action action(Resolver resolver) {
		return new ReturnValue(
				this,
				typeParameters(resolver.getScope()).runtimeValue());
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
	}

	@Override
	public InlineCmd inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public InlineCmd normalize(RootNormalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Cmd cmd(Scope origin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getTargets();
		this.sentences.resolveAll(resolver);
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return NO_DEF_TARGET;
	}

	private static final class FlowSentences extends Sentences {

		private final FlowCommand command;

		FlowSentences(FlowCommand command) {
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
		public List<Sentence> getSentences() {
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

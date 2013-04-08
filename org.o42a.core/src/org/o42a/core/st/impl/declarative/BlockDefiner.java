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

import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentences.inlineBlock;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.ref.*;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.impl.cmd.BlockUtil;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.string.Name;


public final class BlockDefiner extends Definer implements DefinitionsBuilder {

	private final DeclarativeBlockSentences sentences =
			new DeclarativeBlockSentences(this);
	private BlockDefinitions blockDefinitions;

	public BlockDefiner(DeclarativeBlock block, CommandEnv env) {
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
	public final CommandTargets getTargets() {
		return getBlockDefinitions().getTargets();
	}

	@Override
	public Definitions buildDefinitions() {
		return getBlockDefinitions().buildDefinitions();
	}

	@Override
	public DefTarget toTarget(Scope origin) {
		return this.sentences.target(origin);
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
		return BlockUtil.sentencesAction(getBlock(), this, resolver);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
	}

	@Override
	public void resolveTargets(TargetResolver resolver, Scope origin) {
		this.sentences.resolveTargets(resolver, origin);
	}

	@Override
	public InlineEval inline(Normalizer normalizer, Scope origin) {
		return inlineBlock(
				normalizer.getRoot(),
				normalizer,
				origin,
				this.sentences);
	}

	@Override
	public InlineEval normalize(RootNormalizer normalizer, Scope origin) {
		return inlineBlock(normalizer, null, origin, this.sentences);
	}

	@Override
	public Eval eval(Scope origin) {
		assert getStatement().assertFullyResolved();
		return new BlockEval(this, origin);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getTargets();
		this.sentences.resolveAll(resolver);
	}

	private static final class DeclarativeBlockSentences
			extends DeclarativeSentences {

		private final BlockDefiner definer;

		DeclarativeBlockSentences(BlockDefiner definer) {
			this.definer = definer;
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
			return this.definer.getBlock().getSentences();
		}

		@Override
		public CommandTargets getTargets() {
			return this.definer.getTargets();
		}

		@Override
		public String toString() {
			if (this.definer == null) {
				return super.toString();
			}
			return this.definer.toString();
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
			writeSentences(
					dirs,
					host,
					this.origin,
					this.definer.sentences,
					null);
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

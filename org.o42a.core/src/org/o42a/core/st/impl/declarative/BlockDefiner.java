/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.ExecuteInstructions;
import org.o42a.core.st.sentence.*;


public final class BlockDefiner
		extends MainDefiner
		implements DeclarativeSentences {

	static DefValue sentencesValue(
			Resolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {

			final DefValue value = sentence.value(resolver);

			if (value.hasValue()) {
				return value;
			}
			if (!value.getLogicalValue().isTrue()) {
				return value;
			}
		}

		return TRUE_DEF_VALUE;
	}

	static void resolveSentences(
			Resolver resolver,
			DeclarativeSentences sentences) {
		for (DeclarativeSentence sentence : sentences.getSentences()) {
			resolveSentence(resolver, sentence);
		}
	}

	private BlockDefinitions blockDefinitions;

	public BlockDefiner(DeclarativeBlock block, DefinerEnv env) {
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
	public DefValue value(Resolver resolver) {
		return sentencesValue(resolver, this);
	}

	@Override
	public Instruction toInstruction(Resolver resolver) {
		return new ExecuteInstructions(getBlock());
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
	protected void fullyResolve(Resolver resolver) {
		getDefTargets();
		resolveSentences(resolver, this);
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		return new BlockEval(builder, this);
	}

	private BlockDefinitions getBlockDefinitions() {
		if (this.blockDefinitions != null) {
			return this.blockDefinitions;
		}
		return this.blockDefinitions = new BlockDefinitions(this);
	}

	private static void resolveSentence(
			Resolver resolver,
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
			Resolver resolver,
			Declaratives statements) {
		assert statements.assertInstructionsExecuted();
		for (Definer command : statements.getImplications()) {
			command.resolveAll(resolver);
		}
	}

	private static final class BlockEval extends Eval {

		private final BlockDefiner definer;

		BlockEval(CodeBuilder builder, BlockDefiner definer) {
			super(builder, definer.getStatement());
			this.definer = definer;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			writeSentences(dirs, host, this.definer, null);
		}

	}

}

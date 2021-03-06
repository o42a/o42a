/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.cmd.Control.mainControl;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.util.fn.NullableInit.nullableInit;

import java.util.List;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.Scope;
import org.o42a.core.ir.cmd.Cmd;
import org.o42a.core.ir.cmd.Control;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.impl.cmd.InlineSentences;
import org.o42a.core.st.impl.cmd.Sentences;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Sentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.fn.NullableInit;
import org.o42a.util.string.Name;


final class BlockDef extends Def {

	private final DeclarativeBlock block;
	private final CommandEnv env;
	private final DefSentences sentences;
	private final NullableInit<DefTarget> defTarget =
			nullableInit(this::findTarget);
	private InlineSentences normal;

	BlockDef(
			DeclarativeBlock block,
			CommandEnv env,
			CommandTargets targets,
			List<Sentence> sentences) {
		super(
				sourceOf(block),
				block,
				noScopeUpgrade(block.getScope()));
		this.block = block;
		this.env = env;
		this.sentences = new DefSentences(this, targets, sentences);
	}

	private BlockDef(BlockDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.block = prototype.block;
		this.env = prototype.env;
		this.sentences = prototype.sentences;
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	@Override
	public boolean isDefined() {
		return this.sentences.getTargets().haveDefinition();
	}

	@Override
	public boolean isYielding() {
		return this.sentences.getTargets().yielding();
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer) {
		return this.sentences.escapeFlag(analyzer, getScope());
	}

	public final List<Sentence> getSentences() {
		return this.sentences.getSentences();
	}

	@Override
	public DefTarget target() {
		return this.defTarget.get();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.normal = this.sentences.inline(normalizer, null, getScope());
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {

		final InlineSentences inline = this.sentences.inline(
				normalizer.getRoot(),
				normalizer,
				getScope());

		if (inline == null) {
			return null;
		}

		return new CmdEval(inline);
	}

	@Override
	public Eval eval() {

		final Cmd cmd;

		if (this.normal != null) {
			cmd = this.normal;
		} else {
			cmd = this.sentences.cmd(getScope());
		}

		return new CmdEval(cmd);
	}

	@Override
	public String toString() {
		if (this.block == null) {
			return super.toString();
		}
		if (this.sentences == null) {
			return "Definitions[" + this.block + ']';
		}

		final List<Sentence> sentences = this.sentences.getSentences();
		final int len = sentences.size();

		if (len == 0) {
			return ".";
		}

		final StringBuilder out = new StringBuilder();

		out.append(sentences.get(0));
		for (int i = 1; i < len; ++i) {
			out.append(' ').append(sentences.get(i));
		}

		return out.toString();
	}

	@Override
	protected boolean hasConstantValue() {
		return this.sentences.getTargets().isConstant();
	}

	@Override
	protected TypeParameters<?> typeParameters(Scope scope) {

		final TypeParameters<?> expectedParameters =
				this.env.getValueRequest()
				.getExpectedParameters()
				.upgradeScope(scope);

		return this.sentences.typeParameters(scope, expectedParameters);
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return this.sentences.action(this, getBlock(), resolver).toDefValue();
	}

	@Override
	protected void resolveTarget(TargetResolver resolver) {
		this.sentences.resolveTargets(resolver, getScope());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.sentences.resolveAll(resolver);
	}

	@Override
	protected BlockDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new BlockDef(this, upgrade);
	}

	private final DefTarget findTarget() {
		return this.sentences.target(getScope());
	}

	private static final class DefSentences extends Sentences {

		private final BlockDef part;
		private final CommandTargets targets;
		private final List<Sentence> sentences;

		DefSentences(
				BlockDef part,
				CommandTargets targets,
				List<Sentence> sentences) {
			this.part = part;
			this.targets = targets;
			this.sentences = sentences;
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
		public List<Sentence> getSentences() {
			return this.sentences;
		}

		@Override
		public CommandTargets getTargets() {
			return this.targets;
		}

		@Override
		public String toString() {
			if (this.part == null) {
				return super.toString();
			}
			return this.part.toString();
		}

	}

	private static final class CmdEval extends InlineEval {

		private final Cmd cmd;

		CmdEval(Cmd cmd) {
			super(null);
			this.cmd = cmd;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Control control = mainControl(dirs);

			this.cmd.write(control);

			control.end();
		}

		@Override
		public String toString() {
			if (this.cmd == null) {
				return super.toString();
			}
			return this.cmd.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

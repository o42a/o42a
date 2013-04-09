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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentences.inlineBlock;

import java.util.List;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.*;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.fn.Holder;
import org.o42a.util.string.Name;


final class DeclarativePart extends Def {

	private final DeclarativeBlock block;
	private final CommandEnv env;
	private final DeclarativePartSentences sentences;
	private InlineDeclarativeSentences normal;
	private Holder<DefTarget> defTarget;

	DeclarativePart(
			DeclarativeBlock block,
			CommandEnv env,
			CommandTargets targets,
			List<DeclarativeSentence> sentences,
			boolean claim) {
		super(
				sourceOf(block),
				block,
				noScopeUpgrade(block.getScope()),
				claim);
		this.block = block;
		this.env = env;
		this.sentences = new DeclarativePartSentences(this, targets, sentences);
	}

	private DeclarativePart(
			DeclarativePart prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.block = prototype.block;
		this.env = prototype.env;
		this.sentences = prototype.sentences;
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	@Override
	public boolean unconditional() {

		final CommandTargets targets = this.sentences.getTargets();

		return targets.haveValue() && !targets.havePrerequisite();
	}

	public final List<DeclarativeSentence> getSentences() {
		return this.sentences.getSentences();
	}

	@Override
	public DefTarget target() {
		if (this.defTarget != null) {
			return this.defTarget.get();
		}

		final DefTarget defTarget = this.sentences.target(getScope());

		this.defTarget = Holder.holder(defTarget);

		return defTarget;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.normal = inlineBlock(normalizer, null, getScope(), this.sentences);
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {

		final InlineDeclarativeSentences inline = inlineBlock(
				normalizer.getRoot(),
				normalizer,
				getScope(),
				this.sentences);

		if (inline == null) {
			return null;
		}

		return new DeclarativePartEval(this, getScope(), inline);
	}

	@Override
	public Eval eval() {
		return new DeclarativePartEval(this, getScope(), this.normal);
	}

	@Override
	public String toString() {
		if (this.block == null) {
			return super.toString();
		}
		if (this.sentences == null) {
			if (isClaim()) {
				return "Claims[" + this.block + ']';
			}
			return "Propositions[" + this.block + ']';
		}

		final List<DeclarativeSentence> sentences =
				this.sentences.getSentences();
		final int len = sentences.size();

		if (len == 0) {
			return isClaim() ? "!" : ".";
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
	protected DeclarativePart create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new DeclarativePart(this, upgrade);
	}

	private static final class DeclarativePartSentences
			extends DeclarativeSentences {

		private final DeclarativePart part;
		private final CommandTargets targets;
		private final List<DeclarativeSentence> sentences;

		DeclarativePartSentences(
				DeclarativePart part,
				CommandTargets targets,
				List<DeclarativeSentence> sentences) {
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
		public List<DeclarativeSentence> getSentences() {
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

	private static final class DeclarativePartEval extends InlineEval {

		private final DeclarativePart part;
		private final Scope origin;
		private final InlineDeclarativeSentences inlineSentences;

		DeclarativePartEval(
				DeclarativePart part,
				Scope origin,
				InlineDeclarativeSentences inlineSentences) {
			super(null);
			this.part = part;
			this.origin = origin;
			this.inlineSentences = inlineSentences;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			writeSentences(
					dirs,
					host,
					this.origin,
					this.part.sentences,
					this.inlineSentences);
		}

		@Override
		public String toString() {
			if (this.part == null) {
				return super.toString();
			}
			if (this.inlineSentences == null) {
				return this.part.toString();
			}
			return this.inlineSentences.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

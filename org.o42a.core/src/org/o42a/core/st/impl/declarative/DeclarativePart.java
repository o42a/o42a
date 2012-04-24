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
package org.o42a.core.st.impl.declarative;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.impl.declarative.BlockDefiner.*;
import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentences.inlineBlock;

import java.util.List;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefTargets;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.fn.Holder;


final class DeclarativePart extends Def implements DeclarativeSentences {

	private final BlockDefiner definer;
	private final DefTargets targets;
	private final List<DeclarativeSentence> sentences;
	private InlineDeclarativeSentences inline;
	private Holder<DefTarget> defTarget;

	DeclarativePart(
			BlockDefiner definer,
			DefTargets targets,
			List<DeclarativeSentence> sentences,
			boolean claim) {
		super(
				sourceOf(definer),
				definer.getBlock(),
				noScopeUpgrade(definer.getScope()),
				claim);
		this.definer = definer;
		this.sentences = sentences;
		this.targets = targets;
	}

	private DeclarativePart(
			DeclarativePart prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.definer = prototype.definer;
		this.targets = prototype.targets;
		this.sentences = prototype.getSentences();
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {

		final ValueStruct<?, ?> valueStruct =
				this.definer.env().getExpectedValueStruct();

		if (!valueStruct.isScoped()) {
			return valueStruct;
		}

		return valueStruct.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public boolean unconditional() {
		return this.targets.haveValue() && !this.targets.havePrerequisite();
	}

	@Override
	public final DefTargets getDefTargets() {
		return this.targets;
	}

	@Override
	public final List<DeclarativeSentence> getSentences() {
		return this.sentences;
	}

	@Override
	public DefTarget target() {
		if (this.defTarget != null) {
			return this.defTarget.get();
		}

		final DefTarget defTarget = sentencesTargets(this);

		this.defTarget = Holder.holder(defTarget);

		return defTarget;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.inline = inlineBlock(normalizer, null, getScope(), this);
	}

	@Override
	public String toString() {
		if (this.definer == null) {
			return super.toString();
		}
		if (this.sentences == null) {
			if (isClaim()) {
				return "Claims[" + this.definer.getBlock() + ']';
			}
			return "Propositions[" + this.definer.getBlock() + ']';
		}

		final int len = this.sentences.size();

		if (len == 0) {
			return isClaim() ? "!" : ".";
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.sentences.get(0));
		for (int i = 0; i < len; ++i) {
			out.append(' ').append(this.sentences.get(i));
		}

		return out.toString();
	}

	@Override
	protected boolean hasConstantValue() {
		return getDefTargets().isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {

		final DefValue value = sentencesValue(resolver, this);

		if (value.hasValue()) {
			return value.getValue();
		}

		final LogicalValue logicalValue = value.getLogicalValue();

		switch (logicalValue) {
		case TRUE:
			return getValueStruct().unknownValue();
		case FALSE:
			return getValueStruct().falseValue();
		default:
			return getValueStruct().runtimeValue();
		}
	}

	@Override
	protected void resolveTarget(TargetResolver resolver) {
		resolveSentencesTargets(resolver, this);
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		resolveSentences(resolver, this);
	}

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineDeclarativeSentences inline = inlineBlock(
				normalizer.getRoot(),
				normalizer,
				getScope(),
				this);

		if (inline == null) {
			return null;
		}

		return new Inline(valueStruct, inline);
	}

	@Override
	protected ValOp writeDef(ValDirs dirs, HostOp host) {
		return writeSentences(dirs, host, this, this.inline);
	}

	@Override
	protected DeclarativePart create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new DeclarativePart(this, upgrade);
	}

	private final class Inline extends InlineValue {

		private final InlineDeclarativeSentences inlineSentences;

		Inline(
				ValueStruct<?, ?> valueStruct,
				InlineDeclarativeSentences inlineSentences) {
			super(null, valueStruct);
			this.inlineSentences = inlineSentences;
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return writeSentences(
					dirs,
					host,
					DeclarativePart.this,
					this.inlineSentences);
		}

		@Override
		public String toString() {
			if (this.inlineSentences == null) {
				return super.toString();
			}
			return this.inlineSentences.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}

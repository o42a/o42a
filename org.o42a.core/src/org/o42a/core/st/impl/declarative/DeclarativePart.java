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

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.impl.declarative.BlockDefiner.resolveSentences;
import static org.o42a.core.st.impl.declarative.BlockDefiner.sentencesValue;
import static org.o42a.core.st.impl.declarative.DeclarativeOp.writeSentences;
import static org.o42a.core.st.impl.declarative.InlineDeclarativeSentences.inlineBlock;

import java.util.List;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.sentence.DeclarativeSentence;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class DeclarativePart
		extends ValueDef
		implements DeclarativeSentences {

	private final BlockDefiner definer;
	private List<DeclarativeSentence> sentences;
	private InlineDeclarativeSentences inline;

	public DeclarativePart(BlockDefiner definer, boolean claim) {
		super(
				sourceOf(definer),
				definer.getBlock(),
				noScopeUpgrade(definer.getScope()),
				claim);
		this.definer = definer;
	}

	private DeclarativePart(
			DeclarativePart prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.definer = prototype.definer;
		this.sentences = prototype.getSentences();
	}

	@Override
	public final List<DeclarativeSentence> getSentences() {
		if (this.sentences != null) {
			return this.sentences;
		}
		if (isClaim()) {
			return this.sentences = this.definer.getClaims();
		}
		return this.sentences = this.definer.getPropositions();
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.inline = inlineBlock(
				normalizer.newNormalizer(),
				getValueStruct(),
				getScope(),
				this);
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
	public ValueStruct<?, ?> getValueStruct() {
		return this.definer.env().getExpectedValueStruct();
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
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
	protected void fullyResolveDef(Resolver resolver) {
		resolveSentences(resolver, this);
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return inlineBlock(
				normalizer,
				getValueStruct(),
				getScope(),
				this);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return writeSentences(dirs, host, this, this.inline);
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new DeclarativePart(this, upgrade);
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.definer.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.definer.getScope());
	}

	@Override
	protected Logical buildLogical() {
		throw new UnsupportedOperationException();
	}

}

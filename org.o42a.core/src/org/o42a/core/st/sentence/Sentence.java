/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.st.sentence;

import static org.o42a.core.ScopePlace.localPlace;
import static org.o42a.core.ScopePlace.scopePlace;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.*;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueType;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


public abstract class Sentence<S extends Statements<S>> extends Placed {

	private final Block<S> block;
	private final SentenceFactory<S, ?, ?> sentenceFactory;
	private final ArrayList<S> alternatives = new ArrayList<S>();
	private Sentence<S> prerequisite;
	private ValueType<?> valueType;

	Sentence(
			LocationInfo location,
			Block<S> block,
			SentenceFactory<S, ?, ?> sentenceFactory) {
		super(location, new SentenceDistributor(location, block));
		this.block = block;
		this.sentenceFactory = sentenceFactory;
	}

	public Block<S> getBlock() {
		return this.block;
	}

	public MemberRegistry getMemberRegistry() {
		return getBlock().getMemberRegistry();
	}

	public SentenceFactory<S, ?, ?> getSentenceFactory() {
		return this.sentenceFactory;
	}

	public abstract boolean isClaim();

	public abstract boolean isIssue();

	public abstract DefinitionTargets getDefinitionTargets();

	public ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}

		this.valueType = valueType(null);

		if (this.valueType != null) {
			return this.valueType;
		}

		return this.valueType = ValueType.VOID;
	}

	public final List<S> getAlternatives() {
		return this.alternatives;
	}

	public final boolean isEmpty() {
		return getAlternatives().isEmpty();
	}

	public Sentence<S> getPrerequisite() {
		return this.prerequisite;
	}

	public boolean isConditional() {
		if (getPrerequisite() != null) {
			return true;
		}
		return getBlock().isConditional();
	}

	public final S alternative(LocationInfo location) {
		return alternative(location, false);
	}

	public final S opposite(LocationInfo location) {
		return alternative(location, true);
	}

	public S alternative(LocationInfo location, boolean opposite) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final S alternative =
			(S) sentenceFactory.createAlternative(location, this, opposite);

		this.alternatives.add(alternative);

		return alternative;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean separator = false;

		final Sentence<S> prerequisite = getPrerequisite();

		if (prerequisite != null) {
			out.append(prerequisite).append(' ');
		}
		for (Statements<?> alt : getAlternatives()) {
			if (!separator) {
				separator = true;
			} else if (alt.isOpposite()) {
				out.append(" | ");
			} else {
				out.append("; ");
			}
			out.append(alt);
		}
		if (isIssue()) {
			out.append('?');
		} else if (isClaim()) {
			out.append('!');
		} else {
			out.append('.');
		}

		return out.toString();
	}

	final void setPrerequisite(Sentence<S> prerequisite) {
		this.prerequisite = prerequisite;
	}

	ValueType<?> valueType(ValueType<?> expected) {

		ValueType<?> result = expected;
		boolean hasResult = false;

		for (Statements<?> alt : getAlternatives()) {

			final ValueType<?> type = alt.valueType(result);

			if (type == null) {
				continue;
			}
			if (result == null) {
				result = type;
				hasResult = true;
				continue;
			}
			if (type == result) {
				hasResult = true;
				continue;
			}

			getLogger().incompatible(alt, result);
		}

		return hasResult ? result : null;
	}

	void reproduce(Block<S> block, Reproducer reproducer) {

		final Sentence<S> prerequisite = getPrerequisite();

		if (prerequisite != null) {
			prerequisite.reproduce(block, reproducer);
		}

		final Sentence<S> reproduction;

		if (isIssue()) {
			reproduction = block.issue(this);
		} else if (isClaim()) {
			reproduction = block.claim(this);
		} else {
			reproduction = block.propose(this);
		}

		for (S alt : getAlternatives()) {
			alt.reproduce(reproduction, reproducer);
		}
	}

	private static final class SentenceDistributor extends Distributor {

		private final LocationInfo location;
		private final Block<?> block;
		private final ScopePlace place;

		SentenceDistributor(LocationInfo location, Block<?> block) {
			this.location = location;
			this.block = block;

			final Trace trace = this.block.getTrace();

			if (trace == null) {
				this.place = scopePlace(getScope());
			} else {
				this.place = localPlace(getScope().toLocal(), trace.next());
			}
		}

		@Override
		public Loggable getLoggable() {
			return this.location.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.location.getContext();
		}

		@Override
		public Scope getScope() {
			return this.block.getScope();
		}

		@Override
		public Container getContainer() {
			return this.block.getContainer();
		}

		@Override
		public ScopePlace getPlace() {
			return this.place;
		}

	}

}

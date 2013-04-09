/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.*;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandTargets;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;


public abstract class Sentence<S extends Statements<S>> extends Contained {

	private final Block<S> block;
	private final SentenceFactory<S, ?, ?> sentenceFactory;
	private final ArrayList<S> alternatives = new ArrayList<>(1);
	private Sentence<S> prerequisite;
	private boolean statementDropped;
	private boolean instructionsExecuted;

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

	public final boolean isExit() {
		return isClaim() && !getSentenceFactory().isDeclarative();
	}

	public abstract boolean isClaim();

	public abstract boolean isIssue();

	public final boolean isInsideIssue() {
		return isIssue() || getBlock().isInsideIssue();
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

	public abstract CommandTargets getTargets();

	public final S alternative(LocationInfo location) {

		final S alt = createAlt(location);

		if (alt != null) {
			this.alternatives.add(alt);
		} else {
			dropStatement();
		}

		return alt;
	}

	public TypeParameters<?> typeParameters(
			Scope scope,
			TypeParameters<?> expectedParameters) {

		TypeParameters<?> typeParameters = null;

		for (Statements<S> alt : getAlternatives()) {

			final TypeParameters<?> altParameters =
					alt.typeParameters(scope, expectedParameters);

			if (altParameters == null) {
				continue;
			}
			if (typeParameters == null) {
				typeParameters = altParameters;
				continue;
			}
			if (typeParameters.assignableFrom(altParameters)) {
				continue;
			}
			if (altParameters.assignableFrom(typeParameters)) {
				typeParameters = altParameters;
				continue;
			}
			typeParameters = expectedParameters;
		}

		return typeParameters;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		boolean separator = false;

		final Sentence<S> prerequisite = getPrerequisite();

		if (prerequisite != null) {
			out.append(prerequisite).append(' ');
		}
		for (S alt : getAlternatives()) {
			if (!separator) {
				separator = true;
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

	final Sentence<S> firstPrerequisite() {

		Sentence<S> prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return null;
		}
		for (;;) {

			final Sentence<S> prePrerequisite = prerequisite.getPrerequisite();

			if (prePrerequisite == null) {
				return prerequisite;
			}

			prerequisite = prePrerequisite;
		}
	}

	final void executeInstructions() {
		if (this.instructionsExecuted) {
			return;
		}
		this.instructionsExecuted = true;

		final Sentence<S> prerequisite = getPrerequisite();

		if (prerequisite != null) {
			prerequisite.executeInstructions();
		}
		for (S alt : getAlternatives()) {
			alt.executeInstructions();
		}
	}

	final void setPrerequisite(Sentence<S> prerequisite) {
		this.prerequisite = prerequisite;
	}

	final void dropStatement() {
		this.statementDropped = true;
	}

	final void reportEmptyIssue() {
		if (!this.statementDropped) {
			getLogger().warning("prohibited_empty_issue", this, "Impty issue");
		}
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

	private S createAlt(LocationInfo location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final S alt = (S) sentenceFactory.createAlternative(location, this);

		return alt;
	}

	private static final class SentenceDistributor extends Distributor {

		private final Location location;
		private final Block<?> block;
		private final Container container;

		SentenceDistributor(LocationInfo location, Block<?> block) {
			this.location = location.getLocation();
			this.block = block;
			this.container = block.nextContainer();
		}

		@Override
		public Location getLocation() {
			return this.location;
		}

		@Override
		public Scope getScope() {
			return this.block.getScope();
		}

		@Override
		public Container getContainer() {
			return this.container;
		}

	}

}

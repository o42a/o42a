/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.common.phrase;

import java.util.Arrays;
import java.util.HashMap;

import org.o42a.common.phrase.part.PhraseContinuation;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseSubstitution;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.util.ArrayUtil;


public final class ClauseInstance implements LocationInfo {

	private static final PhraseContinuation[] NO_CONTENT =
			new PhraseContinuation[0];

	private final Location location;
	private final PhraseContext context;
	private final HashMap<MemberKey, PhraseSubContext> subContexts =
			new HashMap<>();
	private PhraseContinuation[] content = NO_CONTENT;
	private boolean complete;
	private Definition definition;

	ClauseInstance(PhraseContext context) {
		this(context, context.getLocation());
	}

	ClauseInstance(PhraseContext context, LocationInfo location) {
		this.location = location.getLocation();
		this.context = context;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	public final PhraseContext getPhraseContext() {
		return this.context;
	}

	public final PhraseContinuation[] getContent() {
		return this.content;
	}

	public void addContent(PhraseContinuation part) {
		assert !isComplete() :
			"Can not add content to complete instance: " + part;
		this.content = ArrayUtil.append(this.content, part);
	}

	public final Ref substituteValue(Distributor distributor) {

		final PhraseContinuation[] content = getContent();

		if (content.length != 1) {
			return null;
		}

		return content[0].substitute(distributor);
	}

	public final BlockBuilder getDefinition() {
		if (this.definition != null) {
			return this.definition;
		}
		return this.definition = new Definition(this);
	}

	public void groupDefinition(Block block) {
		getPhraseContext().define(this, block);
		for (PhraseContinuation content : getContent()) {
			content.define(block);
		}
	}

	public Ref instantiateObject(Distributor distributor) {

		final ClauseSubstitution substitution =
				getPhraseContext().getClause().getSubstitution();

		switch (substitution) {
		case NO_SUBSTITUTION:
			break;
		case VALUE_SUBSTITUTION:
			return substituteValue(distributor);
		case PREFIX_SUBSITUTION:
			return getPhraseContext().getPhrase().substitutePrefix(distributor);
		}

		final Path instancePath =
				new ClauseInstanceConstructor(this, distributor).toPath();

		return instancePath.bind(
				new Location(distributor.getContext(), getLocation()),
				distributor.getScope())
				.target(distributor);
	}

	@Override
	public String toString() {
		return this.location + Arrays.toString(this.content);
	}

	final boolean isComplete() {
		return this.complete;
	}

	final void complete() {
		assert !isComplete() :
			"Instance already complete: " + this;
		this.complete = true;
	}

	PhraseSubContext addSubContext(LocationInfo location, Clause clause) {

		final MemberKey key = clause.getKey();
		final PhraseSubContext existing = this.subContexts.get(key);

		if (existing != null) {
			existing.reuse(location);
			return existing;
		}

		final PhraseSubContext context =
				new PhraseSubContext(getPhraseContext(), location, clause);

		this.subContexts.put(key, context);

		return context;
	}

	Iterable<? extends PhraseSubContext> subContexts() {
		return this.subContexts.values();
	}

	final PhraseSubContext subContext(Clause clause) {
		return this.subContexts.get(clause.getKey());
	}

	private static final class Definition extends BlockBuilder {

		private final ClauseInstance instance;

		Definition(ClauseInstance instance) {
			super(instance.getLocation());
			this.instance = instance;
		}

		@Override
		public void buildBlock(Block block) {
			this.instance.getPhraseContext().define(this.instance, block);
			for (PhraseContinuation content : this.instance.getContent()) {
				content.define(block);
			}
		}

		@Override
		public String toString() {
			if (this.instance == null) {
				return super.toString();
			}
			return this.instance.location.toString();
		}

	}

}

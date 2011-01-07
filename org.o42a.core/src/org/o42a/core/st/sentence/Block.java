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

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.def.BlockBase;
import org.o42a.core.member.field.MemberRegistry;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementKind;
import org.o42a.core.value.ValueType;
import org.o42a.util.Place.Trace;


public abstract class Block<S extends Statements<S>> extends BlockBase {

	private final Statements<?> enclosing;
	private Sentence<S> lastIssue;
	private final ArrayList<Sentence<S>> sentences =
		new ArrayList<Sentence<S>>();
	private final MemberRegistry memberRegistry;
	private final SentenceFactory<S, ?, ?> sentenceFactory;
	private StatementKind kind;
	private ValueType<?> valueType;
	private boolean instructionsExecuted;

	protected Block(
			LocationSpec location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			SentenceFactory<S, ?, ?> sentenceFactory) {
		super(location, distributor);
		this.enclosing = null;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
	}

	Block(
			LocationSpec location,
			Distributor distributor,
			Statements<?> enclosing,
			MemberRegistry memberRegistry,
			SentenceFactory<S, ?, ?> sentenceFactory) {
		super(location, distributor);
		this.enclosing = enclosing;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
	}

	public final Statements<?> getEnclosing() {
		return this.enclosing;
	}

	public SentenceFactory<S, ?, ?> getSentenceFactory() {
		return this.sentenceFactory;
	}

	public abstract boolean isParentheses();

	public abstract String getName();

	public MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	public final boolean isConditional() {

		final Statements<?> enclosing = getEnclosing();

		return enclosing != null && enclosing.getSentence().isConditional();
	}

	@Override
	public StatementKind getKind() {
		if (this.kind != null) {
			return this.kind;
		}

		StatementKind result = StatementKind.EMPTY;

		for (Sentence<?> sentence : getSentences()) {

			final StatementKind kind = sentence.getKind();

			if (kind.ordinal() > result.ordinal()) {
				if (kind.hasValue()) {
					return this.kind = StatementKind.VALUE;
				}
				result = kind;
			}
		}

		return this.kind = result;
	}

	@Override
	public ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}
		if (!getKind().hasValue()) {
			return this.valueType = ValueType.VOID;
		}

		ValueType<?> result = null;

		for (Sentence<?> sentence : getSentences()) {

			final ValueType<?> type = sentence.valueType(result);

			if (type == null) {
				continue;
			}
			if (result == null) {
				result = type;
				continue;
			}
			if (result != type) {
				getLogger().incompatible(sentence, result);
			}
		}

		return this.valueType = result;
	}

	public List<? extends Sentence<S>> getSentences() {
		return this.sentences;
	}

	public final Sentence<S> propose(LocationSpec location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S> proposition = sentenceFactory.propose(location, this);

		return addStatementSentence(proposition);
	}

	public final Sentence<S> claim(LocationSpec location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S> claim = sentenceFactory.claim(location, this);

		return addStatementSentence(claim);
	}

	public final Sentence<S> issue(LocationSpec location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S> issue = sentenceFactory.issue(location, this);

		this.sentences.add(issue);
		this.lastIssue = issue;

		return issue;
	}

	public final void executeInstructions() {
		if (this.instructionsExecuted) {
			return;
		}
		this.instructionsExecuted = true;
		for (Sentence<S> sentence : getSentences()) {
			for (S statements : sentence.getAlternatives()) {
				statements.executeInstructions();
			}
		}
	}

	public boolean contains(Block<?> block) {
		if (block == this) {
			return true;
		}

		final Statements<?> enclosing = block.getEnclosing();

		return enclosing != null && contains(enclosing);
	}

	public boolean contains(Statements<?> statements) {
		return contains(statements.getSentence());
	}

	public boolean contains(Sentence<?> sentence) {
		return contains(sentence.getBlock());
	}

	@Override
	public abstract Block<?> reproduce(Reproducer reproducer);

	abstract Trace getTrace();

	void reproduceSentences(Reproducer reproducer, Block<S> reproduction) {
		for (Sentence<S> sentence : getSentences()) {
			sentence.reproduce(reproduction, reproducer);
		}
	}

	Sentence<S> addStatementSentence(Sentence<S> sentence) {
		if (this.lastIssue != null) {
			this.sentences.set(this.sentences.size() - 1, sentence);
			sentence.setPrerequisite(this.lastIssue);
			this.lastIssue = null;
		} else {
			this.sentences.add(sentence);
		}
		return sentence;
	}

}

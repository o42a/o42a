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

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Implication;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.impl.imperative.Locals;
import org.o42a.util.Place.Trace;


public abstract class Block<
		S extends Statements<S, L>,
		L extends Implication<L>>
				extends Statement {

	private final Statements<?, ?> enclosing;
	private final ArrayList<Sentence<S, L>> sentences =
			new ArrayList<Sentence<S, L>>();
	private final MemberRegistry memberRegistry;
	private final SentenceFactory<L, S, ?, ?> sentenceFactory;
	private boolean instructionsExecuted;

	protected Block(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			SentenceFactory<L, S, ?, ?> sentenceFactory) {
		super(location, distributor);
		this.enclosing = null;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
	}

	Block(
			LocationInfo location,
			Distributor distributor,
			Statements<?, ?> enclosing,
			MemberRegistry memberRegistry,
			SentenceFactory<L, S, ?, ?> sentenceFactory) {
		super(location, distributor);
		this.enclosing = enclosing;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
	}

	public Statements<?, ?> getEnclosing() {
		return this.enclosing;
	}

	public SentenceFactory<L, S, ?, ?> getSentenceFactory() {
		return this.sentenceFactory;
	}

	public abstract boolean isParentheses();

	public final boolean isInsideIssue() {

		final Statements<?, ?> enclosing = getEnclosing();

		return enclosing != null && enclosing.isInsideIssue();
	}

	public MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	public final boolean isConditional() {

		final Statements<?, ?> enclosing = getEnclosing();

		return enclosing != null && enclosing.getSentence().isConditional();
	}

	public List<? extends Sentence<S, L>> getSentences() {
		return this.sentences;
	}

	public Sentence<S, L> propose(LocationInfo location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S, L> proposition =
				sentenceFactory.propose(location, this);

		return addSentence(proposition);
	}

	public Sentence<S, L> claim(LocationInfo location) {
		if (isInsideIssue()) {
			if (getSentenceFactory().isDeclarative()) {
				getLogger().error(
						"prohibited_issue_claim",
						location,
						"A claim can not be placed inside an issue");
			} else {
				getLogger().error(
						"prohibited_issue_exit",
						location,
						"Can not exit the loop from inside an issue");
			}
			dropSentence();
			return null;
		}

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S, L> claim = sentenceFactory.claim(location, this);

		return addSentence(claim);
	}

	public Sentence<S, L> issue(LocationInfo location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S, L> issue = sentenceFactory.issue(location, this);

		addSentence(issue);

		return issue;
	}

	public final void executeInstructions() {
		if (this.instructionsExecuted) {
			return;
		}
		this.instructionsExecuted = true;
		for (Sentence<S, L> sentence : getSentences()) {
			sentence.executeInstructions();
		}
	}

	public boolean contains(Block<?, ?> block) {
		if (block == this) {
			return true;
		}

		final Statements<?, ?> enclosing = block.getEnclosing();

		return enclosing != null && contains(enclosing);
	}

	public boolean contains(Statements<?, ?> statements) {
		return contains(statements.getSentence());
	}

	public boolean contains(Sentence<?, ?> sentence) {
		return contains(sentence.getBlock());
	}

	@Override
	public abstract Statement reproduce(Reproducer reproducer);

	public void reproduceSentences(
			Reproducer reproducer,
			Block<S, L> reproduction) {
		for (Sentence<S, L> sentence : getSentences()) {
			sentence.reproduce(reproduction, reproducer);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final boolean parentheses = isParentheses();
		boolean space = false;

		out.append(parentheses ? '(' : '{');
		for (Sentence<S, L> sentence : getSentences()) {
			if (space) {
				out.append(' ');
			} else {
				space = true;
			}
			out.append(sentence);
		}
		out.append(parentheses ? ')' : '}');

		return out.toString();
	}

	abstract Trace getTrace();

	abstract Locals getLocals();

	private Sentence<S, L> addSentence(Sentence<S, L> sentence) {
		if (sentence == null) {
			dropSentence();
			return null;
		}

		final int size = this.sentences.size();

		if (size != 0) {

			final int lastIdx = size - 1;
			final Sentence<S, L> last = this.sentences.get(lastIdx);

			if (last.isIssue()) {
				sentence.setPrerequisite(last);
				this.sentences.set(lastIdx, sentence);
				return sentence;
			}
		}

		this.sentences.add(sentence);

		return sentence;
	}

	private void dropSentence() {

		final Statements<?, ?> enclosing = getEnclosing();

		if (enclosing != null) {
			enclosing.dropStatement();
		}
	}

}

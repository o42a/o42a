/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.BlockDefiner;
import org.o42a.core.st.impl.imperative.Locals;
import org.o42a.util.Place.Trace;


public abstract class Block<S extends Statements<S>> extends Statement {

	private final Statements<?> enclosing;
	private Sentence<S> lastIssue;
	private final ArrayList<Sentence<S>> sentences =
			new ArrayList<Sentence<S>>();
	private final MemberRegistry memberRegistry;
	private final SentenceFactory<S, ?, ?> sentenceFactory;
	private boolean instructionsExecuted;
	private BlockDefiner<?> definer;

	protected Block(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			SentenceFactory<S, ?, ?> sentenceFactory) {
		super(location, distributor);
		this.enclosing = null;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
	}

	Block(
			LocationInfo location,
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

	public List<? extends Sentence<S>> getSentences() {
		return this.sentences;
	}

	public Sentence<S> propose(LocationInfo location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S> proposition = sentenceFactory.propose(location, this);

		return addStatementSentence(proposition);
	}

	public Sentence<S> claim(LocationInfo location) {

		@SuppressWarnings("rawtypes")
		final SentenceFactory sentenceFactory = getSentenceFactory();
		@SuppressWarnings("unchecked")
		final Sentence<S> claim = sentenceFactory.claim(location, this);

		return addStatementSentence(claim);
	}

	public Sentence<S> issue(LocationInfo location) {

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
			sentence.executeInstructions();
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

	public final StatementEnv getInitialEnv() {
		return getDefiner().env();
	}

	@Override
	public final Definer define(StatementEnv env) {
		return this.definer = createDefiner(env);
	}

	@Override
	public abstract Statement reproduce(Reproducer reproducer);

	public void reproduceSentences(
			Reproducer reproducer,
			Block<S> reproduction) {
		for (Sentence<S> sentence : getSentences()) {
			sentence.reproduce(reproduction, reproducer);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final boolean parentheses = isParentheses();
		boolean space = false;

		out.append(parentheses ? '(' : '{');
		for (Sentence<?> sentence : getSentences()) {
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

	@Override
	protected void fullyResolveImperative(LocalResolver resolver) {
		getDefiner().getDefinitionTargets();
		for (Sentence<S> sentence : getSentences()) {
			sentence.resolveImperatives(resolver);
		}
	}

	abstract Trace getTrace();

	abstract Locals getLocals();

	abstract BlockDefiner<?> createDefiner(StatementEnv env);

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

	final BlockDefiner<?> getDefiner() {
		return this.definer;
	}

}

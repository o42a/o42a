/*
    Compiler Core
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
package org.o42a.core.st.sentence;

import static org.o42a.core.st.impl.declarative.DeclarativeBlockCommand.sentencesEscapeMode;

import java.util.ArrayList;
import java.util.List;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.core.st.impl.local.Locals;
import org.o42a.core.value.ValueRequest;
import org.o42a.util.string.Name;


public abstract class Block extends Statement {

	private final Statements enclosing;
	private final ArrayList<Sentence> sentences = new ArrayList<>(1);
	private final MemberRegistry memberRegistry;
	private final SentenceFactory sentenceFactory;
	private final StatementsEnv statementsEnv = new StatementsEnv();
	private final Locals externalLocals;
	private CommandEnv initialEnv;
	private int instructionsExecuted;
	private boolean executingInstructions;

	Block(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry,
			SentenceFactory sentenceFactory) {
		super(location, distributor);
		this.enclosing = null;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
		this.externalLocals = new Locals(this, new BlockLocalFactory(this));
	}

	Block(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			MemberRegistry memberRegistry,
			SentenceFactory sentenceFactory) {
		super(location, distributor);
		this.enclosing = enclosing;
		this.memberRegistry = memberRegistry;
		this.sentenceFactory = sentenceFactory;
		if (enclosing != null) {
			this.externalLocals = enclosing.locals().forBlock(this);
		} else {
			this.externalLocals = new Locals(this, new BlockLocalFactory(this));
		}
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public EscapeMode getEscapeMode() {
		return sentencesEscapeMode(getSentences());
	}

	public final Statements getEnclosing() {
		return this.enclosing;
	}

	public SentenceFactory getSentenceFactory() {
		return this.sentenceFactory;
	}

	public abstract boolean isParentheses();

	public final boolean isImperative() {
		return !getSentenceFactory().isDeclarative();
	}

	public abstract Name getName();

	public final boolean hasName(Name name) {
		if (isParentheses()) {
			return false;
		}
		return name == null || name.equals(getName());
	}

	public final boolean isInterrogation() {

		final Statements enclosing = getEnclosing();

		return enclosing != null && enclosing.isInterrogation();
	}

	public final MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	public final boolean isConditional() {

		final Statements enclosing = getEnclosing();

		return enclosing != null && enclosing.getSentence().isConditional();
	}

	public final List<Sentence> getSentences() {
		return this.sentences;
	}

	public final Sentence declare(LocationInfo location) {

		final Sentence sentence = getSentenceFactory().declare(location, this);

		return addSentence(sentence);
	}

	public final Sentence exit(LocationInfo location) {
		if (isInterrogation()) {
			getLogger().error(
					"prohibited_interrogative_exit",
					location,
					"Can not exit loop from interrogative sentence");
			dropSentence();
			return null;
		}

		final Sentence exit = getSentenceFactory().exit(location, this);

		return addSentence(exit);
	}

	public final Sentence interrogate(LocationInfo location) {

		final Sentence interrogation =
				getSentenceFactory().interrogate(location, this);

		addSentence(interrogation);

		return interrogation;
	}

	public final void executeInstructions() {
		if (this.executingInstructions) {
			return;
		}

		this.executingInstructions = true;
		try {

			final List<Sentence> sentences = getSentences();

			for (int i = this.instructionsExecuted; i < sentences.size(); ++i) {
				this.instructionsExecuted = i + 1;
				sentences.get(i).executeInstructions();
			}
		} finally {
			this.executingInstructions = false;
		}
	}

	public boolean contains(Block block) {
		if (block == this) {
			return true;
		}

		final Statements enclosing = block.getEnclosing();

		return enclosing != null && contains(enclosing);
	}

	public boolean contains(Statements statements) {
		return contains(statements.getSentence());
	}

	public boolean contains(Sentence sentence) {
		return contains(sentence.getBlock());
	}

	@Override
	public final Command command(CommandEnv env) {
		init(env);
		return createCommand(env);
	}

	public void reproduceSentences(Reproducer reproducer, Block reproduction) {
		for (Sentence sentence : getSentences()) {
			sentence.reproduce(reproduction, reproducer);
		}
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final boolean parentheses = isParentheses();
		boolean space = false;

		out.append(parentheses ? '(' : '{');
		for (Sentence sentence : getSentences()) {
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

	final Locals externalLocals() {
		return this.externalLocals;
	}

	abstract NamedBlocks getNamedBlocks();

	abstract Command createCommand(CommandEnv env);

	final CommandEnv statementsEnv() {
		return this.statementsEnv;
	}

	final void init(CommandEnv env) {
		this.initialEnv = env;
	}

	private final CommandEnv getInitialEnv() {
		return this.initialEnv;
	}

	private Sentence addSentence(Sentence sentence) {
		if (sentence == null) {
			dropSentence();
			return null;
		}

		final int size = this.sentences.size();

		if (size != 0) {

			final int lastIdx = size - 1;
			final Sentence last = this.sentences.get(lastIdx);

			if (last.getKind().isInterrogative()) {
				sentence.setPrerequisite(last);
				this.sentences.set(lastIdx, sentence);
				return sentence;
			}
		}

		this.sentences.add(sentence);

		return sentence;
	}

	private void dropSentence() {

		final Statements enclosing = getEnclosing();

		if (enclosing != null) {
			enclosing.dropStatement();
		}
	}

	private final class StatementsEnv extends CommandEnv {

		@Override
		public ValueRequest getValueRequest() {
			return getInitialEnv().getValueRequest();
		}

	}

}

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

import static org.o42a.core.def.Definitions.conditionDefinitions;
import static org.o42a.core.ref.Logical.disjunction;
import static org.o42a.core.st.StatementKinds.NO_STATEMENTS;

import java.util.List;

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.StatementKinds;
import org.o42a.util.log.Loggable;


public abstract class DeclarativeSentence extends Sentence<Declaratives> {

	private InitialConditions initialConditions;
	private SentenceConditions conditions;
	private StatementKinds statementKinds;

	DeclarativeSentence(
			LocationInfo location,
			DeclarativeBlock block,
			DeclarativeFactory sentenceFactory) {
		super(location, block, sentenceFactory);
	}

	@Override
	public DeclarativeBlock getBlock() {
		return (DeclarativeBlock) super.getBlock();
	}

	@Override
	public DeclarativeFactory getSentenceFactory() {
		return (DeclarativeFactory) super.getSentenceFactory();
	}

	@Override
	public DeclarativeSentence getPrerequisite() {
		return (DeclarativeSentence) super.getPrerequisite();
	}

	@Override
	public StatementKinds getStatementKinds() {
		if (this.statementKinds != null) {
			return this.statementKinds;
		}

		StatementKinds result = NO_STATEMENTS;

		for (Declaratives alt : getAlternatives()) {

			final StatementKinds altStatementKinds = alt.getStatementKinds();

			if (altStatementKinds.isEmpty()) {
				continue;
			}
			if (result.isEmpty()) {
				result = altStatementKinds;
				continue;
			}
			if (altStatementKinds.haveDeclaration()) {
				if (!result.haveDeclaration()) {
					getLogger().error(
							"unexpected_declaration",
							alt,
							"Alternative should not contain self-assignment"
							+ " or field declaration");
					continue;
				}
			} else {
				if (result.haveDeclaration()) {
					getLogger().error(
							"expected_declaration",
							alt,
							"Alternative should contain self-assignment"
							+ " or field declaration");
					continue;
				}
			}

			result = result.add(altStatementKinds);
		}

		return this.statementKinds = result;
	}

	protected Definitions define(DefinitionTarget target) {

		final StatementKinds statementKinds = getStatementKinds();

		if (!statementKinds.haveDefinition()) {
			return null;
		}
		if (!statementKinds.haveValue()) {

			final Logical fullLogical =
				getConditions().fullLogical(target.getScope());

			return conditionDefinitions(
					fullLogical,
					target.getScope(),
					fullLogical);
		}

		Loggable previous = null;
		Definitions result = null;

		for (Declaratives alt : getAlternatives()) {

			final StatementKinds altStatementKinds = alt.getStatementKinds();

			if (!altStatementKinds.haveValue()) {
				continue;
			}

			final Definitions definitions = alt.define(target);

			if (result == null) {
				result = definitions;
				previous = alt.getLoggable();
				continue;
			}

			getLogger().error(
					"ambiguous_value",
					alt.getLoggable().setPreviousLoggable(previous),
					"Ambiguous value definition");
		}

		return result;
	}

	Conditions getInitialConditions() {
		if (this.initialConditions != null) {
			return this.initialConditions;
		}
		return this.initialConditions = new InitialConditions(this);
	}

	Conditions getConditions() {
		if (this.conditions != null) {
			return this.conditions;
		}
		return this.conditions = new SentenceConditions(this);
	}

	static final class Proposition extends DeclarativeSentence {

		Proposition(
				LocationInfo location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return false;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

	}

	static final class Claim extends DeclarativeSentence {

		Claim(
				LocationInfo location,
				DeclarativeBlock block,
				DeclarativeFactory sentenceFactory) {
			super(location, block, sentenceFactory);
		}

		@Override
		public boolean isClaim() {
			return true;
		}

		@Override
		public boolean isIssue() {
			return false;
		}

		@Override
		protected Definitions define(DefinitionTarget target) {

			final Definitions definitions = super.define(target);

			if (definitions == null) {
				return null;
			}

			return definitions.claim();
		}

	}

	private static final class InitialConditions extends Conditions {

		private final DeclarativeSentence sentence;

		InitialConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Logical prerequisite(Scope scope) {

			final Conditions initial =
				this.sentence.getBlock().getInitialConditions();
			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite == null) {
				return initial.prerequisite(scope);
			}

			return initial.prerequisite(scope).and(
					prerequisite.getConditions().fullLogical(scope));
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.sentence.getBlock()
			.getInitialConditions().precondition(scope);
		}

		@Override
		public String toString() {

			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite != null) {
				return prerequisite + "? " + this.sentence;
			}

			return this.sentence.toString();
		}

	}

	private static final class SentenceConditions extends Conditions {

		private final DeclarativeSentence sentence;

		SentenceConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.sentence.getInitialConditions().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {

			final List<Declaratives> alternatives =
				this.sentence.getAlternatives();
			final int size = alternatives.size();

			if (size <= 1) {
				if (size == 0) {
					return this.sentence.getInitialConditions()
					.precondition(scope);
				}
				return alternatives.get(0).getConditions().fullLogical(scope);
			}

			final Logical[] vars = new Logical[size];

			for (int i = 0; i < size; ++i) {
				vars[i] =
					alternatives.get(i).getConditions().fullLogical(scope);
			}

			return disjunction(this.sentence, this.sentence.getScope(), vars);
		}

		@Override
		public String toString() {
			return "(" + this.sentence + ")?";
		}

	}

}

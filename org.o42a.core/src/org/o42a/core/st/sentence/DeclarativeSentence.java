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

import static org.o42a.core.def.Definitions.postConditionDefinitions;
import static org.o42a.core.ref.Cond.disjunction;

import java.util.List;

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Cond;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.DefinitionTarget;


public abstract class DeclarativeSentence extends Sentence<Declaratives> {

	private InitialConditions initialConditions;
	private SentenceConditions conditions;

	DeclarativeSentence(
			LocationSpec location,
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

	protected Definitions define(DefinitionTarget target) {
		if (!getKind().hasCondition()) {
			return null;
		}
		if (!getKind().hasDefinition()) {

			final Cond fullCondition =
				getConditions().fullCondition(target.getScope());

			return postConditionDefinitions(
					fullCondition,
					target.getScope(),
					fullCondition);
		}
		for (Declaratives alt : getAlternatives()) {

			final Definitions definitions = alt.define(target);

			if (definitions != null) {
				return definitions;
			}
		}

		return null;
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
				LocationSpec location,
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
				LocationSpec location,
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
		public Cond prerequisite(Scope scope) {

			final Conditions initial =
				this.sentence.getBlock().getInitialConditions();
			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite == null) {
				return initial.prerequisite(scope);
			}

			return initial.prerequisite(scope).and(
					prerequisite.getConditions().fullCondition(scope));
		}

		@Override
		public Cond condition(Scope scope) {
			return this.sentence.getBlock()
			.getInitialConditions().condition(scope);
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
		public Cond prerequisite(Scope scope) {
			return this.sentence.getInitialConditions().prerequisite(scope);
		}

		@Override
		public Cond condition(Scope scope) {

			final List<Declaratives> alternatives =
				this.sentence.getAlternatives();
			final int size = alternatives.size();

			if (size <= 1) {
				if (size == 0) {
					return this.sentence.getInitialConditions()
					.condition(scope);
				}
				return alternatives.get(0).getConditions().fullCondition(scope);
			}

			final Cond[] vars = new Cond[size];

			for (int i = 0; i < size; ++i) {
				vars[i] =
					alternatives.get(i).getConditions().fullCondition(scope);
			}

			return disjunction(this.sentence, this.sentence.getScope(), vars);
		}

		@Override
		public String toString() {
			return "(" + this.sentence + ")?";
		}

	}

}

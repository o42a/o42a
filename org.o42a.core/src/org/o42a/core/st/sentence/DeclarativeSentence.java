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
import static org.o42a.core.ref.Cond.trueCondition;

import java.util.List;

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.MemberKey;
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

	protected Cond condition(Scope scope) {
		if (!getKind().hasCondition()) {
			return null;
		}

		final List<Declaratives> alternatives = getAlternatives();
		final Cond condition;

		if (alternatives.isEmpty()) {
			condition = trueCondition(this, scope);
		} else {

			final Cond[] disjunction = new Cond[alternatives.size()];
			int idx = 0;

			for (Declaratives alt : alternatives) {

				Cond cond = alt.condition(scope);

				if (cond != null) {
					disjunction[idx++] = cond;
				}
			}
			condition = disjunction(this, scope, disjunction);
		}

		final DeclarativeSentence prerequisite = getPrerequisite();

		if (prerequisite == null) {
			return condition;
		}

		final Cond prereq = prerequisite.condition(scope);

		if (isIssue()) {
			return condition.and(prereq);
		}

		return condition.and(prereq).or(prereq.negate());
	}

	protected Definitions define(DefinitionTarget target) {
		if (!getKind().hasCondition()) {
			return null;
		}
		if (!getKind().hasDefinition()) {
			if (target.isField()) {
				return null;
			}

			final Cond condition = condition(target.getScope());

			return postConditionDefinitions(
					condition,
					target.getScope(),
					condition);
		}

		final DeclarativeSentence prerequisite = getPrerequisite();
		final CondDef prereq;

		if (prerequisite == null) {
			prereq = null;
		} else {
			prereq = prerequisite.condition(target.getScope()).toCondDef();
		}

		int oppositionStart = 0;
		int resultIdx = 0;
		Definitions result = null;
		final List<Declaratives> alternatives = getAlternatives();

		for (int i = 0, s = alternatives.size(); i < s; ++i) {

			final Declaratives alt = alternatives.get(i);

			if (result == null) {
				if (oppositionStart < 0 || !alt.isOpposite()) {
					oppositionStart = i;
				}
			}

			final Definitions definitions = alt.define(target);

			if (definitions == null) {
				continue;
			}
			if (result == null) {
				result = definitions.addPrerequisite(prereq);
				resultIdx = i;
				continue;
			}

			final MemberKey memberKey = target.getMemberKey();

			if (memberKey != null) {
				getLogger().ambiguousField(definitions, memberKey.toString());
			} else {
				getLogger().ambiguousValue(definitions);
			}
		}
		if (result == null) {
			return null;
		}
		if (oppositionStart < resultIdx) {

			final Cond opposition =
				opposition(target, oppositionStart, resultIdx);

			result = result.and(opposition.negate());
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

	private Cond opposition(
			DefinitionTarget target,
			int oppositionStart,
			int resultIdx) {

		final List<Declaratives> alternatives = getAlternatives();
		Cond opposition = null;

		for (int i = oppositionStart; i < resultIdx; ++i) {

			final Cond condition =
				alternatives.get(i).condition(target.getScope());

			opposition = Cond.or(opposition, condition);
		}

		return opposition;
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

			return definitions != null ? definitions.claim() : null;
		}

	}

	private static final class InitialConditions extends Conditions {

		private final DeclarativeSentence sentence;
		private Cond prerequisite;

		InitialConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Cond getPrerequisite() {
			if (this.prerequisite != null) {
				return this.prerequisite;
			}

			final Conditions initial =
				this.sentence.getBlock().getInitialConditions();
			final DeclarativeSentence prerequisite =
				this.sentence.getPrerequisite();

			if (prerequisite == null) {
				return this.prerequisite = initial.getPrerequisite();
			}

			return this.prerequisite = initial.getPrerequisite().and(
					prerequisite.getConditions().fullCondition());
		}

		@Override
		public Cond getCondition() {
			return this.sentence.getBlock()
			.getInitialConditions().getCondition();
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
		private Cond condition;

		SentenceConditions(DeclarativeSentence sentence) {
			this.sentence = sentence;
		}

		@Override
		public Cond getPrerequisite() {
			return this.sentence.getInitialConditions().getPrerequisite();
		}

		@Override
		public Cond getCondition() {
			if (this.condition != null) {
				return this.condition;
			}

			final List<Declaratives> alternatives =
				this.sentence.getAlternatives();
			final int size = alternatives.size();

			if (size <= 0) {
				if (size == 0) {
					return this.condition =
						this.sentence.getInitialConditions().getCondition();
				}
				return this.condition =
					alternatives.get(0).getConditions().fullCondition();
			}

			final Cond[] vars = new Cond[size];

			for (int i = 0; i < size; ++i) {
				vars[i] = alternatives.get(i).getConditions().fullCondition();
			}

			return this.condition =
				disjunction(this.sentence, this.sentence.getScope(), vars);
		}

		@Override
		public String toString() {
			return "(" + this.sentence + ")?";
		}

	}

}

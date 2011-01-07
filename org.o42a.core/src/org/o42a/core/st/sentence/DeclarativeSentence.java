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
import org.o42a.core.st.DefinitionTarget;


public abstract class DeclarativeSentence extends Sentence<Declaratives> {

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

}

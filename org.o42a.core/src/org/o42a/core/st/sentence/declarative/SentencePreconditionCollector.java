/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.st.sentence.declarative;

import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;


public final class SentencePreconditionCollector extends SentenceCollector {

	private final SentenceLogicals requirements;
	private final SentenceLogicals conditions;

	private Logical precondition;

	public SentencePreconditionCollector(DeclarativeBlock block, Scope scope) {
		super(block, scope);
		this.requirements = new SentenceLogicals(block, scope);
		this.conditions = new SentenceLogicals(block, scope);
	}

	public Logical precondition() {
		collect();

		final Logical precondition = Logical.and(
				Logical.and(requirement(), condition()),
				this.precondition);

		if (precondition != null) {
			return precondition;
		}

		return getBlock().getInitialEnv().precondition(getScope());
	}

	@Override
	protected void addCondition(
			DeclarativeSentence sentence,
			DefinitionTargets targets) {
		if (sentence.isClaim()) {
			this.requirements.addSentence(sentence);
		} else {
			this.conditions.addSentence(sentence);
		}
	}

	@Override
	protected void addDeclaration(
			DeclarativeSentence sentence,
			DefinitionTargets targets) {

		final Logical logical =
			sentence.getEnv().fullLogical(getScope());

		if (!targets.haveValue()) {
			return;
		}

		this.precondition = Logical.or(this.precondition, logical);
	}

	protected final Logical requirement() {
		return this.requirements.build();
	}

	protected final Logical condition() {
		return this.conditions.build();
	}

}

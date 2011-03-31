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

import java.util.HashMap;

import org.o42a.core.CompilerLogger;
import org.o42a.core.Scope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.DefinitionKey;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.DeclarativeSentence;


public abstract class SentenceCollector {

	private final DeclarativeBlock block;
	private final Scope scope;

	private final HashMap<DefinitionKey, DefinitionTarget> unconditionalValues =
		new HashMap<DefinitionKey, DefinitionTarget>();
	private final SentenceLogicals requirements;
	private final SentenceLogicals conditions;

	public SentenceCollector(DeclarativeBlock block, Scope scope) {
		this.block = block;
		this.scope = scope;
		this.requirements = new SentenceLogicals(block, scope);
		this.conditions = new SentenceLogicals(block, scope);
	}

	public final DeclarativeBlock getBlock() {
		return this.block;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final CompilerLogger getLogger() {
		return getBlock().getLogger();
	}

	protected void collect() {
		for (DeclarativeSentence setence : getBlock().getSentences()) {
			addSentence(setence);
		}
	}

	protected void addSentence(DeclarativeSentence sentence) {
		if (sentence.isIgnored()) {
			return;
		}

		final DefinitionTargets targets = sentence.getDefinitionTargets();

		if (targets.isEmpty()) {
			return;
		}
		if (targets.haveCondition()) {
			updateConditions(sentence);
			addCondition(sentence, targets);
		}
		if (targets.haveDeclaration()) {
			if (!checkAmbiguity(sentence, targets)) {
				return;
			}
			addDeclaration(sentence, targets);
		}
	}

	protected abstract void addCondition(
			DeclarativeSentence sentence,
			DefinitionTargets targets);

	protected abstract void addDeclaration(
			DeclarativeSentence sentence,
			DefinitionTargets targets);

	protected final Logical requirement() {
		return this.requirements.build();
	}

	protected final Logical condition() {
		return this.conditions.build();
	}

	private void updateConditions(DeclarativeSentence sentence) {
		if (sentence.isClaim()) {
			this.requirements.addSentence(sentence);
		} else {
			this.conditions.addSentence(sentence);
		}
	}

	private boolean checkAmbiguity(
			DeclarativeSentence sentence,
			DefinitionTargets targets) {

		boolean result = true;

		for (DefinitionKey definitionKey : targets) {
			if (definitionKey.isDeclaration()) {
				return true;
			}
			if (!checkAmbiguity(sentence, targets, definitionKey)) {
				result = false;
			}
		}

		return result;
	}

	private boolean checkAmbiguity(
			DeclarativeSentence sentence,
			DefinitionTargets targets,
			DefinitionKey definitionKey) {

		final DefinitionTarget unconditionalValue =
			this.unconditionalValues.get(definitionKey);

		if (sentence.getPrerequisite() == null) {
			if (unconditionalValue == null) {
				this.unconditionalValues.put(
						definitionKey,
						targets.first(definitionKey));
				return true;
			}
			getLogger().error(
					"ambiguous_value",
					targets.firstValue().getLoggable()
					.setPreviousLoggable(
							unconditionalValue.getLoggable()),
					"Ambigous value");
			sentence.ignore();
			return false;
		}
		if (unconditionalValue == null) {
			return true;
		}

		getLogger().error(
				"ignored_value",
				targets.firstValue().getLoggable().setPreviousLoggable(
						unconditionalValue.getLoggable()),
				"Ignored value");
		sentence.ignore();

		return false;
	}

}

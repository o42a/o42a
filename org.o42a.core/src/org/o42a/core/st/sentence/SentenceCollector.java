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
package org.o42a.core.st.sentence;

import static org.o42a.core.def.Definitions.emptyDefinitions;

import java.util.HashMap;

import org.o42a.core.CompilerLogger;
import org.o42a.core.Scope;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.DefinitionKey;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.DefinitionTargets;


abstract class SentenceCollector {

	private final DeclarativeBlock block;
	private final Scope scope;

	private final HashMap<DefinitionKey, DefinitionTarget>unconditionalValues =
		new HashMap<DefinitionKey, DefinitionTarget>();
	private Logical commonReq = null;
	private Logical reqs = null;
	private Logical req = null;
	private Logical commonCond = null;
	private Logical conds = null;
	private Logical cond = null;

	public SentenceCollector(DeclarativeBlock block, Scope scope) {
		this.block = block;
		this.scope = scope;
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
		return Logical.and(this.commonReq, Logical.or(this.req, this.reqs));
	}

	protected final Logical condition() {
		return Logical.and(this.commonCond, Logical.or(this.cond, this.conds));
	}

	private void updateConditions(DeclarativeSentence sentence) {

		final Logical logical =
			sentence.getConditions().fullLogical(getScope());

		if (sentence.isClaim()) {
			if (sentence.getPrerequisite() == null) {
				this.req = Logical.and(this.req, logical);
				return;
			}
			if (this.req == null) {
				this.reqs = Logical.or(this.reqs, logical);
				return;
			}
			this.commonReq = requirement();
			this.reqs = logical;
			this.req = null;
			return;
		}
		if (sentence.getPrerequisite() == null) {
			this.cond = Logical.and(this.cond, logical);
			return;
		}
		if (this.cond == null) {
			this.conds = Logical.or(this.conds, logical);
			return;
		}
		this.commonCond = condition();
		this.conds = logical;
		this.cond = null;
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

	static final class DefinitionsCollector extends SentenceCollector {

		private Definitions definitions;

		public DefinitionsCollector(DeclarativeBlock block, Scope scope) {
			super(block, scope);
		}

		public Definitions definitions() {
			collect();
			if (this.definitions == null) {
				return null;
			}
			this.definitions = this.definitions.addRequirement(requirement());
			this.definitions = this.definitions.addCondition(condition());
			return this.definitions;
		}

		@Override
		protected void addCondition(
				DeclarativeSentence sentence,
				DefinitionTargets targets) {
			if (this.definitions == null) {
				this.definitions = emptyDefinitions(getBlock(), getScope());
			}
		}

		@Override
		protected void addDeclaration(
				DeclarativeSentence sentence,
				DefinitionTargets targets) {
			if (!targets.haveValue()) {
				return;
			}

			final Definitions definitions = sentence.define(getScope());

			assert definitions != null :
				sentence + " has no definitions";

			if (this.definitions == null) {
				this.definitions = definitions;
			} else {
				this.definitions = this.definitions.refine(definitions);
			}
		}

	}

	static final class PreconditionCollector extends SentenceCollector {

		private Logical precondition;

		public PreconditionCollector(DeclarativeBlock block, Scope scope) {
			super(block, scope);
		}

		public Logical precondition() {
			collect();

			final Logical precondition = Logical.and(
					Logical.and(requirement(), condition()),
					this.precondition);

			if (precondition != null) {
				return precondition;
			}

			return getBlock().getInitialConditions().precondition(getScope());
		}

		@Override
		protected void addCondition(
				DeclarativeSentence sentence,
				DefinitionTargets targets) {
		}

		@Override
		protected void addDeclaration(
				DeclarativeSentence sentence,
				DefinitionTargets targets) {

			final Logical logical =
				sentence.getConditions().fullLogical(getScope());

			if (!targets.haveValue()) {
				return;
			}

			this.precondition = Logical.or(this.precondition, logical);
		}

	}

}

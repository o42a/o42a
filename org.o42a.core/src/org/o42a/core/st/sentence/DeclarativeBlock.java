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

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.core.ref.Logical.disjunction;
import static org.o42a.core.st.Conditions.emptyConditions;
import static org.o42a.core.st.sentence.SentenceFactory.DECLARATIVE_FACTORY;

import java.util.List;

import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Conditions;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.DefinitionTargets;
import org.o42a.core.st.action.Action;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Place.Trace;


public final class DeclarativeBlock extends Block<Declaratives> {

	static DeclarativeBlock declarativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			DeclarativeFactory sentenceFactory) {
		return new DeclarativeBlock(
				location,
				distributor,
				enclosing,
				sentenceFactory);
	}

	private BlockConditions conditions;

	public DeclarativeBlock(
			LocationInfo location,
			Container container,
			MemberRegistry memberRegistry) {
		this(
				location,
				declarativeDistributor(container),
				memberRegistry);
	}

	public DeclarativeBlock(
			LocationInfo location,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		super(
				location,
				distributor,
				memberRegistry,
				DECLARATIVE_FACTORY);
	}

	public DeclarativeBlock(
			LocationInfo location,
			DeclaredField<?, ?> field,
			Statements<?> enclosing,
			MemberRegistry memberRegistry) {
		super(
				location,
				declarativeDistributor(field.getContainer()),
				enclosing,
				memberRegistry,
				DECLARATIVE_FACTORY);
	}

	private DeclarativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			DeclarativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				enclosing.getMemberRegistry(),
				sentenceFactory);
	}

	@Override
	public boolean isParentheses() {
		return true;
	}

	@Override
	public String getName() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DeclarativeSentence> getSentences() {
		return (List<DeclarativeSentence>) super.getSentences();
	}

	@Override
	public final DeclarativeBlock toDeclarativeBlock() {
		return this;
	}

	@Override
	public final DeclarativeSentence propose(LocationInfo location) {
		return (DeclarativeSentence) super.propose(location);
	}

	@Override
	public final DeclarativeSentence claim(LocationInfo location) {
		return (DeclarativeSentence) super.claim(location);
	}

	@Override
	public final DeclarativeSentence issue(LocationInfo location) {
		return (DeclarativeSentence) super.issue(location);
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		assert this.conditions == null :
			"Conditions already set for " + this;
		return this.conditions = new BlockConditions(this, conditions);
	}

	@Override
	public Definitions define(Scope scope) {
		if (!getDefinitionTargets().haveDefinition()) {
			return null;
		}

		final List<DeclarativeSentence> sentences = getSentences();
		Definitions result = null;
		Logical prereq = null;
		Logical elseReq = null;
		Logical reqs = null;
		Logical precond = null;
		Logical elseCond = null;
		Logical conds = null;

		for (DeclarativeSentence sentence : sentences) {

			final DefinitionTargets kinds = sentence.getDefinitionTargets();

			if (!kinds.haveDefinition()) {
				continue;
			}
			if (kinds.haveValue()) {

				final Definitions definitions = sentence.define(scope);

				assert definitions != null :
					sentence + " has no definitions";

				if (result == null) {
					result = definitions;
				} else {
					result = result.refine(definitions);
				}

				continue;
			}

			final Logical logical =
				sentence.getConditions().fullLogical(scope.getScope());

			if (sentence.isClaim()) {
				if (sentence.getPrerequisite() != null) {
					reqs = Logical.or(reqs, logical);
				} else if (reqs == null) {
					prereq = Logical.and(prereq, logical);
				} else {
					elseReq = Logical.and(elseReq, logical);
				}
			} else {
				if (sentence.getPrerequisite() != null) {
					conds = Logical.or(conds, logical);
				} else if (conds == null) {
					precond = Logical.and(precond, logical);
				} else {
					elseCond = Logical.and(elseCond, logical);
				}
			}

			if (result == null) {
				result = emptyDefinitions(this, scope.getScope());
			}
		}
		if (result == null) {
			return null;
		}

		result = result.addRequirement(
				Logical.and(prereq, Logical.or(elseReq, reqs)));
		result = result.addCondition(
				Logical.and(precond, Logical.or(elseCond, conds)));

		return result;
	}

	@Override
	public Action initialValue(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DeclarativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final DeclarativeBlock reproduction;

		if (getEnclosing() == null) {
			reproduction = new DeclarativeBlock(
					this,
					reproducer.getContainer(),
					reproducer.getMemberRegistry());
			reproduceSentences(reproducer, reproduction);
			return reproduction;
		}

		reproduction =
			(DeclarativeBlock) reproducer.getStatements().parentheses(this);
		reproduceSentences(reproducer, reproduction);

		return null;
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	@Override
	final Trace getTrace() {
		return null;
	}

	final Conditions getInitialConditions() {
		if (this.conditions != null) {
			return this.conditions.initialConditions;
		}

		final Conditions initial = emptyConditions(this);

		this.conditions = new BlockConditions(this, initial);

		return initial;
	}

	private static final class BlockConditions extends Conditions {

		private final Conditions initialConditions;
		private final DeclarativeBlock block;

		BlockConditions(DeclarativeBlock block, Conditions initialConditions) {
			this.initialConditions = initialConditions;
			this.block = block;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.initialConditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {

			final List<DeclarativeSentence> sentences =
				this.block.getSentences();
			final int size = sentences.size();

			if (size <= 0) {
				return this.initialConditions.precondition(scope);
			}

			Logical req = null;
			Logical vars[] = new Logical[size + 1];
			int varIdx = 0;

			for (DeclarativeSentence sentence : sentences) {

				final Logical fullLogical =
					sentence.getConditions().fullLogical(scope);

				if (sentence.getPrerequisite() != null) {
					vars[varIdx++] = fullLogical;
				} else if (req == null) {
					req = fullLogical;
				} else {
					req = req.and(fullLogical);
				}
			}

			if (varIdx == 0) {
				if (req == null) {
					return this.initialConditions.precondition(scope);
				}
				return req;
			}
			if (req != null) {
				vars[varIdx++] = req;
			}

			return disjunction(
					this.block,
					this.block.getScope(),
					ArrayUtil.clip(vars, varIdx));
		}

		@Override
		public String toString() {
			return "BlockConditions[" + this.block + ']';
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.initialConditions.getExpectedType();
		}

	}

}

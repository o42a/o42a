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
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.action.Action;
import org.o42a.util.ArrayUtil;
import org.o42a.util.Place.Trace;
import org.o42a.util.log.Loggable;


public final class DeclarativeBlock extends Block<Declaratives> {

	static DeclarativeBlock declarativeBlock(
			LocationSpec location,
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
			LocationSpec location,
			Container container,
			MemberRegistry memberRegistry) {
		this(
				location,
				declarativeDistributor(container),
				memberRegistry);
	}

	public DeclarativeBlock(
			LocationSpec location,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		super(
				location,
				distributor,
				memberRegistry,
				DECLARATIVE_FACTORY);
	}

	public DeclarativeBlock(
			LocationSpec location,
			DeclaredField<?> field,
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
			LocationSpec location,
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
	public Conditions setConditions(Conditions conditions) {
		assert this.conditions == null :
			"Conditions already set for " + this;
		return this.conditions = new BlockConditions(conditions, this);
	}

	@Override
	public Definitions define(DefinitionTarget target) {
		if (!getKind().hasLogicalValue()) {
			return null;
		}

		final List<DeclarativeSentence> sentences = getSentences();
		Definitions result = null;

		for (DeclarativeSentence sentence : sentences) {

			final Definitions definitions = sentence.define(target);

			if (definitions == null) {
				continue;
			}

			if (result == null) {
				result = definitions;
			} else {
				result = result.refine(definitions);
			}
		}

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
	public String toString() {

		final StringBuilder out = new StringBuilder();
		final Loggable loggable = getLoggable();

		if (loggable != null) {
			loggable.printContent(out);
			if (out.length() < 2 || out.charAt(0) != '(') {
				out.insert(0, '(').append(')');
			}
		} else {
			out.append("(...)");
		}

		return out.toString();
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
			return this.conditions.conditions;
		}

		final Conditions initial = emptyConditions(this);

		this.conditions = new BlockConditions(initial, this);

		return initial;
	}

	private static final class BlockConditions extends Conditions {

		private final Conditions conditions;
		private final DeclarativeBlock block;

		BlockConditions(Conditions conditions, DeclarativeBlock block) {
			this.conditions = conditions;
			this.block = block;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.conditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {

			final List<DeclarativeSentence> sentences =
				this.block.getSentences();
			final int size = sentences.size();

			if (size <= 0) {
				return this.conditions.precondition(scope);
			}

			Logical req = null;
			final Logical[] vars = new Logical[size];
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
					return this.conditions.precondition(scope);
				}
				return req;
			}

			final Logical disjunction = disjunction(
					this.block,
					this.block.getScope(),
					ArrayUtil.clip(vars, varIdx));

			if (req == null) {
				return disjunction;
			}

			return req.and(disjunction);
		}

		@Override
		public String toString() {
			return "BlockConditions[" + this.block + ']';
		}

	}

}

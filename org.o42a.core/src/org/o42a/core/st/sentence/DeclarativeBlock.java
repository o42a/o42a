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
import static org.o42a.core.st.StatementEnv.defaultEnv;
import static org.o42a.core.st.sentence.SentenceFactory.DECLARATIVE_FACTORY;

import java.util.List;

import org.o42a.core.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.Logical;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.sentence.declarative.ImplicitInclusion;
import org.o42a.core.st.sentence.declarative.SentencePrecondition;
import org.o42a.core.st.sentence.imperative.Locals;
import org.o42a.core.value.ValueType;
import org.o42a.util.Place.Trace;


public final class DeclarativeBlock extends Block<Declaratives> {

	static DeclarativeBlock nestedBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			DeclarativeFactory sentenceFactory) {
		return new DeclarativeBlock(
				location,
				distributor,
				enclosing,
				enclosing.getMemberRegistry(),
				sentenceFactory,
				false);
	}

	private BlockEnv env;
	private Locals locals;

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
		this(
				location,
				distributor,
				null,
				memberRegistry,
				DECLARATIVE_FACTORY,
				false);
	}

	public DeclarativeBlock(
			LocationInfo location,
			DeclaredField<?, ?> field,
			Statements<?> enclosing,
			MemberRegistry memberRegistry) {
		this(
				location,
				declarativeDistributor(field.getContainer()),
				enclosing,
				memberRegistry,
				DECLARATIVE_FACTORY,
				false);
	}

	private DeclarativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements<?> enclosing,
			MemberRegistry memberRegistry,
			DeclarativeFactory sentenceFactory,
			boolean reproduced) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		if (!reproduced) {
			addImplicitInclusions();
		}
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
	public StatementEnv setEnv(StatementEnv env) {
		assert this.env == null :
			"Environment already assigned to " + this;
		return this.env = new BlockEnv(this, env);
	}

	@Override
	public Definitions define(Scope scope) {
		if (!getDefinitionTargets().haveDefinition()) {
			return null;
		}

		Definitions result = null;

		for (DeclarativeSentence sentence : getSentences()) {

			final Definitions definitions = sentence.define(scope);

			if (definitions == null) {
				continue;
			}
			if (result == null) {
				result = definitions;
			} else {
				result = result.refine(definitions);
			}
		}

		assert result != null :
			"Missing definitions: " + this;

		return result;
	}

	@Override
	public Action initialValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action initialLogicalValue(LocalResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DeclarativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final DeclarativeBlock reproduction;

		if (getEnclosing() == null) {
			reproduction = new DeclarativeBlock(
					this,
					declarativeDistributor(reproducer.getContainer()),
					null,
					reproducer.getMemberRegistry(),
					DECLARATIVE_FACTORY,
					true);
			reproduceSentences(reproducer, reproduction);
			return reproduction;
		}

		reproduction =
			(DeclarativeBlock) reproducer.getStatements().parentheses(this);
		reproduceSentences(reproducer, reproduction);

		return null;
	}

	public final StatementEnv getInitialEnv() {
		if (this.env != null) {
			return this.env.initialEnv;
		}

		final StatementEnv initial = defaultEnv(this);

		this.env = new BlockEnv(this, initial);

		return initial;
	}

	@Override
	protected StOp createOp(LocalBuilder builder) {
		throw new UnsupportedOperationException();
	}

	@Override
	final Trace getTrace() {
		return null;
	}

	@Override
	Locals getLocals() {
		if (this.locals != null) {
			return this.locals;
		}

		final Statements<?> enclosing = getEnclosing();

		if (enclosing == null) {
			return this.locals = new Locals(this);
		}

		return this.locals = enclosing.getSentence().getBlock().getLocals();
	}

	private void addImplicitInclusions() {
		if (getEnclosing() != null) {
			return;
		}
		if (!getMemberRegistry().inclusions().inclusionsSupported()) {
			return;
		}
		if (!getContext().getSectionTag().isDefault()) {
			// Enclosing context is a section.
			// Only explicit (tagged) inclusions supported.
			return;
		}

		final Declaratives statements = propose(this).alternative(this);

		statements.statement(new ImplicitInclusion(this, statements));
	}

	private static final class BlockEnv extends StatementEnv {

		private final StatementEnv initialEnv;
		private final DeclarativeBlock block;

		BlockEnv(DeclarativeBlock block, StatementEnv initialEnv) {
			this.initialEnv = initialEnv;
			this.block = block;
		}

		@Override
		public boolean hasPrerequisite() {
			return this.initialEnv.hasPrerequisite();
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.initialEnv.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {

			final SentencePrecondition collector =
				new SentencePrecondition(this.block, scope);

			return collector.precondition();
		}

		@Override
		public String toString() {
			return "BlockEnv[" + this.block + ']';
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.initialEnv.getExpectedType();
		}

	}

}

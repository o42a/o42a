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

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.impl.BlockDefiner;
import org.o42a.core.st.impl.declarative.DeclarativeBlockDefiner;
import org.o42a.core.st.impl.declarative.ImplicitInclusion;
import org.o42a.core.st.impl.imperative.Locals;
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
	public DeclarativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements<?> enclosing = reproducer.getStatements();
		final DeclarativeBlock reproduction;

		if (enclosing == null) {
			reproduction = new DeclarativeBlock(
					this,
					declarativeDistributor(reproducer.getContainer()),
					null,
					reproducer.getMemberRegistry(),
					DECLARATIVE_FACTORY,
					true);
			reproduction.define(defaultEnv(this));
			reproduceSentences(reproducer, reproduction);
			return null;
		}

		reproduction = (DeclarativeBlock) enclosing.parentheses(this);
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

	@Override
	BlockDefiner<?> createDefiner(StatementEnv env) {
		return new DeclarativeBlockDefiner(this, env);
	}

	private void addImplicitInclusions() {
		if (getEnclosing() != null) {
			return;
		}
		if (!getMemberRegistry().inclusions().implicitInclusionsSupported()) {
			return;
		}
		if (!getContext().getSectionTag().isImplicit()) {
			// Enclosing context is a section.
			// Only explicit (tagged) inclusions supported.
			return;
		}

		final Declaratives statements = propose(this).alternative(this);

		statements.statement(new ImplicitInclusion(this, statements));
	}

}

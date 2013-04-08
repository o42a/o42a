/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.Distributor.containerDistributor;
import static org.o42a.core.st.CommandEnv.defaultEnv;
import static org.o42a.core.st.sentence.SentenceFactory.DECLARATIVE_FACTORY;

import java.util.List;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Definer;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.impl.declarative.BlockDefiner;
import org.o42a.core.st.impl.declarative.DeclarativeBlockCommand;
import org.o42a.core.st.impl.declarative.ImplicitInclusion;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.util.string.Name;


public final class DeclarativeBlock extends Block<Declaratives, Definer> {

	static DeclarativeBlock nestedBlock(
			LocationInfo location,
			Distributor distributor,
			Declaratives enclosing,
			DeclarativeFactory sentenceFactory) {
		return new DeclarativeBlock(
				location,
				distributor,
				enclosing,
				enclosing.getMemberRegistry(),
				sentenceFactory,
				false);
	}

	private NamedBlocks namedBlocks;

	public DeclarativeBlock(
			LocationInfo location,
			Container container,
			MemberRegistry memberRegistry) {
		this(
				location,
				containerDistributor(container),
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
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		this(
				group,
				distributor,
				(Declaratives) group.getStatements(),
				memberRegistry,
				DECLARATIVE_FACTORY,
				false);
	}

	private DeclarativeBlock(
			LocationInfo location,
			Distributor distributor,
			Declaratives enclosing,
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
	public Declaratives getEnclosing() {
		return (Declaratives) super.getEnclosing();
	}

	@Override
	public final boolean isParentheses() {
		return true;
	}

	@Override
	public final Name getName() {
		return null;
	}

	public final boolean isInsideClaim() {

		final Declaratives enclosing = getEnclosing();

		return enclosing != null && enclosing.isInsideClaim();
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

	public DefinitionsBuilder definitions(CommandEnv env) {
		init(env);
		return createCommand(env);
	}

	@Override
	public DeclarativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements<?, ?> enclosing = reproducer.getStatements();
		final DeclarativeBlock reproduction;

		if (enclosing == null) {
			reproduction = new DeclarativeBlock(
					this,
					containerDistributor(reproducer.getContainer()),
					null,
					reproducer.getMemberRegistry(),
					DECLARATIVE_FACTORY,
					true);
			reproduction.define(defaultEnv(reproducer.getLogger()));
			reproduceSentences(reproducer, reproduction);
			return null;
		}

		reproduction = enclosing.parentheses(this).toDeclarativeBlock();
		reproduceSentences(reproducer, reproduction);

		return null;
	}

	@Override
	NamedBlocks getNamedBlocks() {
		if (this.namedBlocks != null) {
			return this.namedBlocks;
		}

		final Declaratives enclosing = getEnclosing();

		if (enclosing == null) {
			return this.namedBlocks = new NamedBlocks(this);
		}

		return this.namedBlocks =
				enclosing.getSentence().getBlock().getNamedBlocks();
	}

	@Override
	final BlockDefiner createDefiner(CommandEnv env) {
		return new BlockDefiner(this, env);
	}

	@Override
	final DeclarativeBlockCommand createCommand(CommandEnv env) {
		return new DeclarativeBlockCommand(this, env);
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

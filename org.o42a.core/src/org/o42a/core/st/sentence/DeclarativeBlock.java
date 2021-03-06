/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.impl.declarative.DeclarativeBlockCommand;
import org.o42a.core.st.impl.declarative.Inclusion;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.util.fn.Init;
import org.o42a.util.string.Name;


public final class DeclarativeBlock extends Block {

	static DeclarativeBlock nestedBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			DeclarativeFactory sentenceFactory) {
		return new DeclarativeBlock(
				location,
				distributor,
				enclosing,
				enclosing.getMemberRegistry(),
				sentenceFactory,
				false);
	}

	private final Init<NamedBlocks> namedBlocks =
			Init.init(this::createNamedBlocks);

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
				group.getStatements(),
				memberRegistry,
				DECLARATIVE_FACTORY,
				false);
	}

	private DeclarativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
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
	public final boolean isParentheses() {
		return true;
	}

	@Override
	public final Name getName() {
		return null;
	}

	public DefinitionsBuilder definitions(CommandEnv env) {
		init(env);
		return createCommand(env);
	}

	@Override
	public Block reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements enclosing = reproducer.getStatements();

		if (enclosing != null) {
			final Block reproduction = enclosing.parentheses(this);

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final Block reproduction = new DeclarativeBlock(
				this,
				containerDistributor(reproducer.getContainer()),
				null,
				reproducer.getMemberRegistry(),
				DECLARATIVE_FACTORY,
				true);

		reproduction.command(defaultEnv(reproducer.getLogger()));
		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	@Override
	final NamedBlocks getNamedBlocks() {
		return this.namedBlocks.get();
	}

	@Override
	final DeclarativeBlockCommand createCommand(CommandEnv env) {
		return new DeclarativeBlockCommand(this, env);
	}

	private NamedBlocks createNamedBlocks() {

		final Statements enclosing = getEnclosing();

		if (enclosing == null) {
			return new NamedBlocks(this);
		}

		return enclosing.getSentence().getBlock().getNamedBlocks();
	}

	private void addImplicitInclusions() {
		if (getEnclosing() != null) {
			return;
		}
		if (!getMemberRegistry().inclusions().hasInclusions()) {
			return;
		}

		final Statements statements = declare(this).alternative(this);

		statements.statement(new Inclusion(this, statements));
	}

}

/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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

import static org.o42a.core.st.CommandEnv.defaultEnv;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.impl.flow.FlowCommand;
import org.o42a.core.st.impl.imperative.ImperativeMemberRegistry;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.util.string.Name;


public final class FlowBlock extends Block {

	public static FlowBlock flowBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			Name name,
			ImperativeFactory sentenceFactory,
			MemberRegistry memberRegistry) {

		final Scope scope = enclosing.getScope();

		if (scope == null) {
			return null;
		}

		final MemberRegistry registry;

		if (memberRegistry != null) {
			registry = memberRegistry;
		} else {
			registry =
					new ImperativeMemberRegistry(enclosing.getMemberRegistry());
		}

		return new FlowBlock(
				location,
				distributor,
				enclosing,
				name,
				registry,
				sentenceFactory);
	}

	private Name name;
	private NamedBlocks namedBlocks;

	private FlowBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			Name name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(location, distributor, enclosing, memberRegistry, sentenceFactory);
		this.name = name;
	}

	private FlowBlock(
			FlowBlock prototype,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(prototype, distributor, memberRegistry, sentenceFactory);
		this.name = prototype.getName();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@Override
	public final boolean isParentheses() {
		return false;
	}

	@Override
	public final Name getName() {
		return this.name;
	}

	@Override
	public FlowBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements enclosing = reproducer.getStatements();

		if (enclosing != null) {

			final FlowBlock reproduction = enclosing.flow(this, getName());

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final FlowBlock reproduction = new FlowBlock(
				this,
				reproducer.distribute(),
				reproducer.getMemberRegistry(),
				getSentenceFactory().toImperativeFactory());

		reproduction.command(defaultEnv(reproducer.getLogger()));
		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	@Override
	NamedBlocks getNamedBlocks() {
		if (this.namedBlocks != null) {
			return this.namedBlocks;
		}
		return this.namedBlocks =
				getEnclosing().getSentence().getBlock().getNamedBlocks();
	}

	@Override
	Command createCommand(CommandEnv env) {
		return new FlowCommand(this, env);
	}

}

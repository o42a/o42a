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

import static org.o42a.core.st.CommandEnv.defaultEnv;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Command;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.impl.imperative.ImperativeBlockCommand;
import org.o42a.core.st.impl.imperative.ImperativeMemberRegistry;
import org.o42a.core.st.impl.imperative.NamedBlocks;
import org.o42a.util.fn.Init;
import org.o42a.util.string.Name;


public final class ImperativeBlock extends Block {

	public static ImperativeBlock topLevelImperativeBlock(
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

		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				false,
				name,
				registry,
				sentenceFactory);
	}

	public static ImperativeBlock nestedImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			boolean parentheses,
			Name name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		return new ImperativeBlock(
				location,
				distributor,
				enclosing,
				parentheses,
				name,
				memberRegistry,
				sentenceFactory);
	}

	private final boolean parentheses;
	private final Name name;
	private final Init<NamedBlocks> namedBlocks = Init.init(
			() -> getEnclosing().getSentence().getBlock().getNamedBlocks());

	private ImperativeBlock(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			boolean parentheses,
			Name name,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(
				location,
				distributor,
				enclosing,
				memberRegistry,
				sentenceFactory);
		this.parentheses = parentheses;
		this.name = name;
	}

	private ImperativeBlock(
			ImperativeBlock prototype,
			Distributor distributor,
			MemberRegistry memberRegistry,
			ImperativeFactory sentenceFactory) {
		super(prototype, distributor, memberRegistry, sentenceFactory);
		this.parentheses = false;
		this.name = prototype.getName();
	}

	@Override
	public final ImperativeFactory getSentenceFactory() {
		return (ImperativeFactory) super.getSentenceFactory();
	}

	@Override
	public final boolean isParentheses() {
		return this.parentheses;
	}

	@Override
	public final Name getName() {
		return this.name;
	}

	@Override
	public ImperativeBlock reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Statements enclosing = reproducer.getStatements();

		if (enclosing != null) {

			final ImperativeBlock reproduction =
					enclosing.braces(this, getName());

			reproduceSentences(reproducer, reproduction);

			return null;
		}

		final ImperativeBlock reproduction = new ImperativeBlock(
				this,
				reproducer.distribute(),
				reproducer.getMemberRegistry(),
				getSentenceFactory());

		reproduction.command(defaultEnv(reproducer.getLogger()));
		reproduceSentences(reproducer, reproduction);

		return reproduction;
	}

	@Override
	final NamedBlocks getNamedBlocks() {
		return this.namedBlocks.get();
	}

	@Override
	final Command createCommand(CommandEnv env) {
		return new ImperativeBlockCommand(this, env);
	}

}

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

import static org.o42a.core.st.sentence.DeclarativeBlock.nestedBlock;
import static org.o42a.core.st.sentence.ImperativeBlock.topLevelImperativeBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.impl.DefaultSentence;
import org.o42a.core.st.impl.InterrogativeSentence;
import org.o42a.util.string.Name;


public class DeclarativeFactory extends SentenceFactory {

	protected DeclarativeFactory() {
	}

	@Override
	public DeclarativeBlock createParentheses(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing) {
		return nestedBlock(location, distributor, enclosing, this);
	}

	@Override
	public DeclarativeBlock groupParentheses(
			Group group,
			Distributor distributor,
			MemberRegistry memberRegistry) {
		return new DeclarativeBlock(group, distributor, memberRegistry);
	}

	@Override
	public ImperativeBlock groupBraces(
			Group group,
			Distributor distributor,
			Name name,
			MemberRegistry memberRegistry) {
		return topLevelImperativeBlock(
				group,
				distributor,
				group.getStatements(),
				name,
				IMPERATIVE_GROUP_FACTORY,
				memberRegistry);
	}

	@Override
	public ImperativeBlock createBraces(
			LocationInfo location,
			Distributor distributor,
			Statements enclosing,
			Name name) {
		return topLevelImperativeBlock(
				location,
				distributor,
				enclosing,
				name,
				IMPERATIVE_FACTORY,
				null);
	}

	@Override
	public Sentence interrogate(LocationInfo location, Block block) {
		return new InterrogativeSentence(
				location,
				block,
				DECLARATIVE_INTERROGATION_FACTORY);
	}

	@Override
	public Sentence declare(LocationInfo location, Block block) {
		return new DefaultSentence(location, block, this);
	}

	@Override
	public Sentence exit(LocationInfo location, Block block) {
		location.getLocation().getLogger().error(
				"prohibited_loop_exit",
				location,
				"No loop to exit");
		return new DefaultSentence(location, block, this);
	}

	@Override
	public final DeclarativeFactory toDeclarativeFactory() {
		return this;
	}

	@Override
	public final ImperativeFactory toImperativeFactory() {
		return null;
	}

}
